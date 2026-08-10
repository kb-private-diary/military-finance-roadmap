package org.scoula.travel.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.travel.domain.TravelPackageVO;

@Log4j2
@Component
public class YellowBalloonClient {

    private static final String SEARCH_URL =
            "https://pkg.ybtour.co.kr/search/searchKeyword.ajax"
                    + "?collection=ybtour&viewCount=1000&sort="
                    + "&keyword=&categoryNo=&departWDay=&departMonth="
                    + "&departTime=&departCity=&period=&lowPrice="
                    + "&highPrice=&guidYn=&startDate=&endDate="
                    + "&locationCd=&bestYn=&cityList=&query=";
    private static final String EVENT_URL =
            "https://papi.ybtour.co.kr/pkg/event/by-goods/";
    private static final String DETAIL_URL =
            "https://prdt.ybtour.co.kr/product/detailPackage?menu=PKG";
    private static final DateTimeFormatter API_DATE_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE;
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;
    private static final int MAX_RESULTS = 10;
    private static final Map<String, List<String>> CITY_ALIASES = Map.of(
            "호치민", List.of("호치민", "호찌민", "SGN"),
            "도쿄", List.of("도쿄", "동경", "NRT", "HND"),
            "교토", List.of("교토", "교토시", "KIX", "ITM"));

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TravelPackageVO> searchPackages(
            final String country,
            final String destination,
            final LocalDate departureDate) {
        if (departureDate == null) {
            return List.of();
        }

        try {
            final JsonNode products = this.searchProducts(
                    destination, departureDate);
            final List<PackageCandidate> candidates = new ArrayList<>();
            for (final JsonNode product : products) {
                this.addDepartureCandidates(
                        candidates,
                        product,
                        departureDate);
            }
            final List<TravelPackageVO> packages = this.toPackages(
                    candidates, country, destination);

            if (packages.isEmpty()) {
                throw BusinessException.notFound(
                        "추천할 수 있는 패키지 상품이 없습니다.",
                        "TRAVEL_031");
            }
            return packages;
        } catch (final IOException | IllegalArgumentException exception) {
            log.warn("노랑풍선 검색 요청 오류: destination={}",
                    destination, exception);
            throw this.packageServiceUnavailable();
        }
    }

    private JsonNode searchProducts(
            final String destination,
            final LocalDate departureDate)
            throws IOException {
        final String encodedDestination = URLEncoder.encode(
                destination, StandardCharsets.UTF_8);
        final String requestUrl = SEARCH_URL
                + encodedDestination
                + "&departDate="
                + departureDate.format(API_DATE_FORMAT);
        final JsonNode response = this.requestJson(
                requestUrl,
                "https://pkg.ybtour.co.kr/search/searchPdt.yb?query="
                        + encodedDestination);
        final JsonNode products = response.path("pdtList");
        return products.isArray()
                ? products
                : this.objectMapper.createArrayNode();
    }

    private void addDepartureCandidates(
            final List<PackageCandidate> candidates,
            final JsonNode product,
            final LocalDate departureDate) {
        final String goodsCode = this.text(product, "goodsCd");
        final String displayId = this.findDisplayId(product);
        if (goodsCode.isEmpty() || displayId.isEmpty()) {
            log.debug("패키지 일정 조회 식별값 누락: goodsCode={}, dspSid={}",
                    goodsCode, displayId);
            return;
        }

        try {
            final String month = departureDate.format(
                    DateTimeFormatter.ofPattern("yyyyMM"));
            final String eventUrl = EVENT_URL
                    + goodsCode
                    + "/"
                    + this.toMonthlyDisplayId(displayId)
                    + "/"
                    + month;
            final JsonNode events = this.requestJson(eventUrl).path("body");
            if (!events.isArray()) {
                return;
            }

            for (final JsonNode event : events) {
                if (!this.isSameDepartureDate(event, departureDate)) {
                    continue;
                }

                final String eventCode = this.text(event, "evCd");
                if (eventCode.isEmpty()) {
                    continue;
                }
                final long price = this.findCalendarPrice(event);
                if (price <= 0) {
                    continue;
                }
                candidates.add(new PackageCandidate(
                        product, event, displayId, price));
            }
        } catch (final IOException exception) {
            log.warn("노랑풍선 상품 일정 조회 실패: goodsCode={}, date={}",
                    goodsCode, departureDate, exception);
        }
    }

    private List<TravelPackageVO> toPackages(
            final List<PackageCandidate> candidates,
            final String country,
            final String destination) {
        final List<TravelPackageVO> packages = new ArrayList<>();
        final Set<String> selectedGoodsCodes = new HashSet<>();
        candidates.sort(Comparator.comparingLong(PackageCandidate::price));

        for (final PackageCandidate candidate : candidates) {
            if (packages.size() >= MAX_RESULTS) {
                break;
            }
            final String goodsCode = this.text(
                    candidate.product(), "goodsCd");
            if (selectedGoodsCodes.contains(goodsCode)) {
                continue;
            }
            final String eventCode = this.text(
                    candidate.event(), "evCd");
            final JsonNode detail = this.findEventDetailSafely(
                    candidate.displayId(), eventCode);
            final String tourPath = this.firstText(
                    candidate.event().path("tourPatInfo"),
                    detail.path("tourPatInfo"));
            final String destinationEvidence = String.join(
                    " ",
                    tourPath,
                    this.text(candidate.product(), "goodsNm"),
                    this.text(candidate.product(), "pidNm"),
                    this.text(detail, "evNm"));
            if (!this.includesDestination(
                    destinationEvidence, destination)) {
                continue;
            }
            selectedGoodsCodes.add(goodsCode);
            packages.add(this.toPackage(
                    candidate.product(),
                    detail,
                    country,
                    destination,
                    eventCode,
                    candidate.price(),
                    candidate.displayId(),
                    candidate.event()));
        }
        return packages;
    }

    private JsonNode findEventDetail(
            final String displayId,
            final String eventCode)
            throws IOException {
        final String requestUrl = DETAIL_URL
                + "&dspSid="
                + URLEncoder.encode(displayId, StandardCharsets.UTF_8)
                + "&evCd="
                + URLEncoder.encode(eventCode, StandardCharsets.UTF_8);
        final String html = this.request(requestUrl, "text/html");
        final String marker =
                "<script id=\"__NEXT_DATA__\" type=\"application/json\">";
        final int jsonStart = html.indexOf(marker);
        if (jsonStart < 0) {
            return this.objectMapper.createObjectNode();
        }
        final int contentStart = jsonStart + marker.length();
        final int jsonEnd = html.indexOf("</script>", contentStart);
        if (jsonEnd < 0) {
            return this.objectMapper.createObjectNode();
        }
        return this.objectMapper.readTree(
                html.substring(contentStart, jsonEnd))
                .path("props")
                .path("pageProps")
                .path("eventDetail");
    }

    private JsonNode findEventDetailSafely(
            final String displayId,
            final String eventCode) {
        try {
            return this.findEventDetail(displayId, eventCode);
        } catch (final IOException exception) {
            log.warn("노랑풍선 상품 상세 조회 실패: evCd={}",
                    eventCode, exception);
            return this.objectMapper.createObjectNode();
        }
    }

    private long findCalendarPrice(final JsonNode event) {
        return event.path("adtPrice").asLong(0L)
                + event.path("bafAdtPrice").asLong(0L)
                + event.path("airTaxAdtPrice").asLong(0L);
    }

    private TravelPackageVO toPackage(
            final JsonNode product,
            final JsonNode detail,
            final String country,
            final String destination,
            final String eventCode,
            final long price,
            final String displayId,
            final JsonNode event) {
        final String fullName = this.firstText(
                detail.path("evNm"), product.path("goodsNm"));
        final int descriptionStart = fullName.indexOf('#');
        final String name = descriptionStart < 0
                ? fullName
                : fullName.substring(0, descriptionStart).trim();
        final String description = descriptionStart < 0
                ? ""
                : fullName.substring(descriptionStart).trim();
        final String departureDate = this.firstText(
                event.path("outStartDt"),
                event.path("evStartDt"));
        final String arrivalDate = this.firstText(
                event.path("inArriveDt"),
                detail.path("evArriveDt"));

        return TravelPackageVO.builder()
                .goodsCode(eventCode)
                .country(country)
                .regionName(destination)
                .name(name)
                .imageUrl(this.firstText(
                        product.path("imageUrl"),
                        product.path("imageThum3")))
                .description(description)
                .minPrice(price)
                .departurePeriod(this.formatDeparturePeriod(
                        departureDate, arrivalDate))
                .isActive(true)
                .detailUrl(DETAIL_URL
                        + "&dspSid="
                        + displayId
                        + "&evCd="
                        + eventCode)
                .build();
    }

    private boolean isSameDepartureDate(
            final JsonNode event,
            final LocalDate departureDate) {
        return departureDate.format(API_DATE_FORMAT)
                .equals(this.firstText(
                        event.path("outStartDt"),
                        event.path("evStartDt")));
    }

    private boolean includesDestination(
            final String tourPath,
            final String destination) {
        if (tourPath.isBlank()) {
            return false;
        }
        final String normalizedPath = this.normalize(tourPath);
        return CITY_ALIASES.getOrDefault(
                destination, List.of(destination))
                .stream()
                .map(this::normalize)
                .anyMatch(normalizedPath::contains);
    }

    private String findDisplayId(final JsonNode product) {
        for (final String field : List.of(
                "dspSid4", "dspSid", "dspSid3", "dspSid2",
                "categoryNo")) {
            final String value = this.text(product, field);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String toMonthlyDisplayId(final String displayId) {
        if (displayId.length() < 2) {
            return displayId;
        }
        return displayId.substring(0, displayId.length() - 2) + "00";
    }

    private String normalize(final String value) {
        return value.toLowerCase(Locale.KOREAN)
                .replaceAll("[^0-9a-z가-힣]", "");
    }

    private JsonNode requestJson(final String url)
            throws IOException {
        return this.objectMapper.readTree(
                this.request(url, "application/json"));
    }

    private JsonNode requestJson(
            final String url,
            final String referer) throws IOException {
        return this.objectMapper.readTree(
                this.request(url, "application/json", referer));
    }

    private String request(final String url, final String accept)
            throws IOException {
        return this.request(
                url,
                accept,
                "https://pkg.ybtour.co.kr/search/searchPdt.yb");
    }

    private String request(
            final String url,
            final String accept,
            final String referer) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty(
                    "Accept-Language", "ko-KR,ko;q=0.9");
            connection.setRequestProperty(
                    "X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("Referer", referer);

            final int statusCode = connection.getResponseCode();
            if (statusCode < HttpURLConnection.HTTP_OK
                    || statusCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new IOException(
                        "Yellow Balloon response status: " + statusCode);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                return new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String text(final JsonNode node, final String field) {
        return node.path(field).asText("").trim();
    }

    private String firstText(final JsonNode first, final JsonNode second) {
        final String firstValue = first.asText("").trim();
        return firstValue.isEmpty()
                ? second.asText("").trim()
                : firstValue;
    }

    private String formatDeparturePeriod(
            final String startDate,
            final String arrivalDate) {
        return this.formatDate(startDate)
                + " ~ "
                + this.formatDate(arrivalDate);
    }

    private String formatDate(final String date) {
        if (date.length() != 8) {
            return date;
        }
        return date.substring(0, 4)
                + "."
                + date.substring(4, 6)
                + "."
                + date.substring(6, 8);
    }

    private BusinessException packageServiceUnavailable() {
        return new BusinessException(
                "패키지 상품 서비스를 이용할 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "TRAVEL_030");
    }

    private record PackageCandidate(
            JsonNode product,
            JsonNode event,
            String displayId,
            long price) {
    }
}

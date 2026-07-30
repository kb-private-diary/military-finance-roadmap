package org.scoula.travel.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.travel.domain.TravelPackageVO;

@Log4j2
@Component
public class YellowBalloonClient {

    private static final String SEARCH_URL =
            "https://pkg.ybtour.co.kr/search/searchKeyword.ajax"
                    + "?collection=ybtour&viewCount=10&sort=AMDASC&keyword="
                    + "&categoryNo=&departWDay=&departMonth="
                    + "&departTime=&departCity=&period=&lowPrice="
                    + "&highPrice=&guidYn=&startDate=&endDate="
                    + "&locationCd=&bestYn=&cityList=&query=";
    private static final String DETAIL_URL =
            "https://prdt.ybtour.co.kr/product/detailPackage"
                    + "?menu=PKG&goodsCd=";
    private static final int MAX_RESULTS = 10;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TravelPackageVO> searchPackages(
            final String country,
            final String destination,
            final LocalDate departureDate) {
        final String encodedDestination = URLEncoder.encode(
                destination, StandardCharsets.UTF_8);
        final String formattedDepartureDate = departureDate == null
                ? ""
                : departureDate.format(
                        DateTimeFormatter.BASIC_ISO_DATE);
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SEARCH_URL
                        + encodedDestination
                        + "&departDate="
                        + formattedDepartureDate))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; TravelRoadmap/1.0)")
                .header("Accept", "application/json")
                .header("Accept-Language", "ko-KR,ko;q=0.9")
                .header("Referer",
                        "https://pkg.ybtour.co.kr/search/searchPdt.yb")
                .GET()
                .build();

        try {
            final HttpResponse<String> response = this.httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));
            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                log.warn("노랑풍선 검색 실패: status={}, destination={}",
                        response.statusCode(), destination);
                throw this.packageServiceUnavailable();
            }

            final List<TravelPackageVO> packages = this.parsePackages(
                    response.body(), country, destination);
            if (packages.isEmpty()) {
                throw BusinessException.notFound(
                        "추천할 수 있는 패키지 상품이 없습니다.",
                        "TRAVEL_031");
            }
            return packages;
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw this.packageServiceUnavailable();
        } catch (final IOException | IllegalArgumentException exception) {
            log.warn("노랑풍선 검색 요청 오류: destination={}",
                    destination, exception);
            throw this.packageServiceUnavailable();
        }
    }

    private List<TravelPackageVO> parsePackages(
            final String json,
            final String country,
            final String destination) throws IOException {
        final List<TravelPackageVO> packages = new ArrayList<>();
        final JsonNode products =
                this.objectMapper.readTree(json).path("pdtList");
        if (!products.isArray()) {
            return packages;
        }

        for (final JsonNode product : products) {
            if (packages.size() >= MAX_RESULTS) {
                break;
            }

            final String goodsCode =
                    product.path("goodsCd").asText("").trim();
            final String fullName =
                    product.path("goodsNm").asText("").trim();
            final long minPrice = product.path("minPrice").asLong(0L);
            if (goodsCode.isEmpty()
                    || fullName.isEmpty()
                    || minPrice <= 0) {
                continue;
            }

            final int descriptionStart = fullName.indexOf('#');
            final String name = descriptionStart < 0
                    ? fullName
                    : fullName.substring(0, descriptionStart).trim();
            final String description = descriptionStart < 0
                    ? ""
                    : fullName.substring(descriptionStart).trim();

            packages.add(TravelPackageVO.builder()
                    .goodsCode(goodsCode)
                    .country(country)
                    .regionName(product.path("goodsAreaTwoNm")
                            .asText(destination))
                    .name(name)
                    .imageUrl(product.path("imageUrl").asText(""))
                    .description(description)
                    .minPrice(minPrice)
                    .departurePeriod(this.formatDeparturePeriod(
                            product.path("firstStartDate").asText(""),
                            product.path("lastStartDate").asText("")))
                    .isActive(true)
                    .detailUrl(DETAIL_URL + goodsCode)
                    .build());
        }
        return packages;
    }

    private String formatDeparturePeriod(
            final String firstStartDate,
            final String lastStartDate) {
        return this.formatDate(firstStartDate)
                + " ~ "
                + this.formatDate(lastStartDate);
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
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "TRAVEL_030");
    }
}

package org.scoula.travel.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.travel.util.TravelPriceCalculator;
import org.scoula.travel.util.TravelPriceDistribution;

/**
 * RapidAPI의 Booking COM 호텔 검색 클라이언트.
 *
 * <p>도시의 목적지 ID는 메모리에 캐시하고, 성인 1명·객실 1개·KRW 조건으로
 * 검색된 호텔 총액의 중앙값을 일반 기준 숙박비로 사용한다.</p>
 */
@Log4j2
@Component
public class BookingApiClient {

    private static final String DESTINATION_PATH =
            "/api/v1/hotels/searchDestination";
    private static final String HOTEL_SEARCH_PATH =
            "/api/v1/hotels/searchHotels";
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 25_000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, DestinationInfo> destinationCache =
            new ConcurrentHashMap<>();

    @Value("${booking.api-key}")
    private String apiKey;

    @Value("${booking.api-url}")
    private String apiUrl;

    public long estimateHotelCost(
            final String country,
            final String city,
            final LocalDate checkInDate,
            final LocalDate checkOutDate) {
        return this.estimateHotelCosts(
                country, city, checkInDate, checkOutDate)
                .getCommonCost();
    }

    public TravelPriceDistribution estimateHotelCosts(
            final String country,
            final String city,
            final LocalDate checkInDate,
            final LocalDate checkOutDate) {
        this.validateRequest(country, city, checkInDate, checkOutDate);

        // 출발일과 도착일이 같으면 숙박하지 않는 당일 여행으로 본다.
        if (checkInDate.equals(checkOutDate)) {
            return TravelPriceDistribution.fixed(0L);
        }
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw this.bookingApiUnavailable();
        }

        final String cacheKey = country + ":" + city;
        DestinationInfo destination = this.destinationCache.get(cacheKey);
        if (destination == null) {
            destination = this.findDestination(country, city);
            this.destinationCache.put(cacheKey, destination);
        }

        final List<BigDecimal> prices = this.findHotelPrices(
                destination, checkInDate, checkOutDate);
        return TravelPriceCalculator.findPriceDistribution(prices);
    }

    private void validateRequest(
            String country,
            String city,
            LocalDate checkInDate,
            LocalDate checkOutDate) {
        if (country == null || country.trim().isEmpty()
                || city == null || city.trim().isEmpty()) {
            throw BusinessException.badRequest(
                    "선택할 수 없는 도착지입니다.",
                    "TRAVEL_009");
        }
        if (checkInDate == null) {
            throw BusinessException.badRequest(
                    "출발일은 필수입니다.",
                    "TRAVEL_001");
        }
        if (checkOutDate == null) {
            throw BusinessException.badRequest(
                    "도착일은 필수입니다.",
                    "TRAVEL_004");
        }
        if (checkOutDate.isBefore(checkInDate)) {
            throw BusinessException.badRequest(
                    "도착일이 출발일보다 빠를 수 없습니다.",
                    "TRAVEL_002");
        }
    }

    private DestinationInfo findDestination(String country, String city) {
        String query = city;
        String body = this.get(
                DESTINATION_PATH + "?query=" + this.encode(query));

        try {
            JsonNode destinations = this.objectMapper.readTree(body).path("data");
            if (!destinations.isArray() || destinations.size() == 0) {
                log.warn(
                        "Booking 목적지 검색 결과 없음: country={}, city={}",
                        country,
                        city);
                throw this.hotelNotFound();
            }

            JsonNode selected = destinations.get(0);
            for (JsonNode destination : destinations) {
                if ("CITY".equalsIgnoreCase(
                        destination.path("search_type").asText())) {
                    selected = destination;
                    break;
                }
            }

            String destinationId = selected.path("dest_id").asText();
            String searchType = selected.path("search_type").asText();
            if (destinationId.isEmpty() || searchType.isEmpty()) {
                throw this.bookingApiUnavailable();
            }
            return new DestinationInfo(destinationId, searchType);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Booking 목적지 응답 파싱 오류", e);
            throw this.bookingApiUnavailable();
        }
    }

    private List<BigDecimal> findHotelPrices(
            DestinationInfo destination,
            LocalDate checkInDate,
            LocalDate checkOutDate) {
        String path = HOTEL_SEARCH_PATH
                + "?dest_id=" + this.encode(destination.destinationId)
                + "&search_type=" + this.encode(destination.searchType)
                + "&arrival_date=" + checkInDate
                + "&departure_date=" + checkOutDate
                + "&adults=1"
                + "&room_qty=1"
                + "&page_number=1"
                + "&languagecode=en-us"
                + "&currency_code=KRW";
        String body = this.get(path);

        try {
            JsonNode root = this.objectMapper.readTree(body);
            if (!root.path("status").asBoolean(true)) {
                log.warn(
                        "Booking 호텔 검색 실패: message={}",
                        root.path("message"));
                throw this.bookingApiUnavailable();
            }

            JsonNode hotels = root.path("data").path("hotels");
            if (!hotels.isArray() || hotels.size() == 0) {
                log.warn(
                        "Booking 호텔 검색 결과 없음: destId={}, searchType={}, "
                                + "checkInDate={}, checkOutDate={}, data={}",
                        destination.destinationId,
                        destination.searchType,
                        checkInDate,
                        checkOutDate,
                        root.path("data"));
                throw this.hotelNotFound();
            }

            List<BigDecimal> prices = new ArrayList<>();
            for (JsonNode hotel : hotels) {
                JsonNode priceBreakdown = hotel.path("property")
                        .path("priceBreakdown");
                if (priceBreakdown.isMissingNode()
                        || priceBreakdown.isNull()) {
                    priceBreakdown = hotel.path("priceBreakdown");
                }
                JsonNode value = priceBreakdown.path("grossPrice").path("value");
                if (value.isNumber() || value.isTextual()) {
                    BigDecimal price = new BigDecimal(value.asText());
                    if (price.signum() > 0) {
                        prices.add(price);
                    }
                }
            }

            if (prices.isEmpty()) {
                log.warn(
                        "Booking 호텔 가격 정보 없음: hotelCount={}, "
                                + "firstHotel={}",
                        hotels.size(),
                        hotels.get(0));
                throw this.hotelNotFound();
            }
            return prices;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | NumberFormatException e) {
            log.warn("Booking 호텔 응답 파싱 오류", e);
            throw this.bookingApiUnavailable();
        }
    }

    private String get(String path) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.apiUrl + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("X-RapidAPI-Key", this.apiKey);
            connection.setRequestProperty(
                    "X-RapidAPI-Host",
                    new URL(this.apiUrl).getHost());
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);

            int status = connection.getResponseCode();
            String body = this.readBody(
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream());
            if (status == 429) {
                throw this.bookingApiLimitExceeded();
            }
            if (status < 200 || status >= 300) {
                log.warn("Booking API 호출 실패: status={}, body={}", status, body);
                throw this.bookingApiUnavailable();
            }
            return body;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Booking API 통신 오류", e);
            throw this.bookingApiUnavailable();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    private BusinessException bookingApiUnavailable() {
        return new BusinessException(
                "숙박비 서비스를 이용할 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "TRAVEL_020");
    }

    private BusinessException hotelNotFound() {
        return BusinessException.notFound(
                "조회 가능한 숙박시설이 없습니다.",
                "TRAVEL_021");
    }

    private BusinessException bookingApiLimitExceeded() {
        return new BusinessException(
                "숙박 API 호출 한도를 초과했습니다.",
                HttpStatus.TOO_MANY_REQUESTS,
                "TRAVEL_022");
    }

    private static final class DestinationInfo {

        private final String destinationId;
        private final String searchType;

        private DestinationInfo(String destinationId, String searchType) {
            this.destinationId = destinationId;
            this.searchType = searchType;
        }
    }
}

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
import java.util.Locale;
import java.util.regex.Pattern;

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
 * FlightAPI.io 왕복 항공권 가격 클라이언트.
 */
@Log4j2
@Component
public class FlightApiClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 30_000;
    private static final Pattern IATA_CODE_PATTERN =
            Pattern.compile("^[A-Z]{3}$");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${flightprice.api-key}")
    private String apiKey;

    @Value("${flightprice.api-url}")
    private String apiUrl;

    public long estimateRoundTripCost(
            final String departureAirport,
            final String destinationAirport,
            final LocalDate departureDate,
            final LocalDate returnDate) {
        return this.estimateRoundTripCosts(
                departureAirport,
                destinationAirport,
                departureDate,
                returnDate).getCommonCost();
    }

    public TravelPriceDistribution estimateRoundTripCosts(
            final String departureAirport,
            final String destinationAirport,
            final LocalDate departureDate,
            final LocalDate returnDate) {
        this.validateRequest(departureDate, returnDate);
        this.validateAirportCode(departureAirport);
        this.validateAirportCode(destinationAirport);
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw this.flightApiUnavailable();
        }

        final String response = this.fetch(
                departureAirport, destinationAirport, departureDate, returnDate);
        final List<BigDecimal> prices = this.extractPrices(response);
        return TravelPriceCalculator.findPriceDistribution(prices);
    }

    private void validateRequest(LocalDate departureDate, LocalDate returnDate) {
        if (departureDate == null) {
            throw BusinessException.badRequest(
                    "출발일은 필수입니다.",
                    "TRAVEL_001");
        }
        if (returnDate == null) {
            throw BusinessException.badRequest(
                    "도착일은 필수입니다.",
                    "TRAVEL_004");
        }
        if (returnDate.isBefore(departureDate)) {
            throw BusinessException.badRequest(
                    "도착일이 출발일보다 빠를 수 없습니다.",
                    "TRAVEL_002");
        }
    }

    private void validateAirportCode(final String airportCode) {
        if (airportCode == null
                || !IATA_CODE_PATTERN.matcher(airportCode).matches()) {
            throw BusinessException.badRequest(
                    "유효하지 않은 공항 코드입니다.",
                    "TRAVEL_016");
        }
    }

    private String fetch(
            String departureAirport,
            String destinationAirport,
            LocalDate departureDate,
            LocalDate returnDate) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.buildUrl(
                    departureAirport, destinationAirport,
                    departureDate, returnDate));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);

            int status = connection.getResponseCode();
            String body = this.readBody(
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream());
            if (status < 200 || status >= 300) {
                // API 키가 URL 경로에 있으므로 전체 요청 URL은 절대 로그에 남기지 않는다.
                log.warn("FlightAPI 호출 실패: status={}, body={}", status, body);
                throw this.toApiException(status, body);
            }
            return body;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.warn("FlightAPI 통신 오류", e);
            throw this.flightApiUnavailable();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildUrl(
            String departureAirport,
            String destinationAirport,
            LocalDate departureDate,
            LocalDate returnDate) {
        return this.apiUrl
                + "/" + this.encodePath(this.apiKey)
                + "/" + departureAirport
                + "/" + destinationAirport
                + "/" + departureDate
                + "/" + returnDate
                + "/1/0/0/Economy/KRW";
    }

    private String encodePath(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private List<BigDecimal> extractPrices(String body) {
        try {
            JsonNode root = this.objectMapper.readTree(body);
            JsonNode itineraries = root.path("itineraries");
            if (!itineraries.isArray() || itineraries.size() == 0) {
                throw this.flightNotFound();
            }

            List<BigDecimal> prices = new ArrayList<>();
            for (JsonNode itinerary : itineraries) {
                this.addPrice(prices, itinerary.path("cheapest_price"));

                JsonNode pricingOptions = itinerary.path("pricing_options");
                if (!pricingOptions.isArray()) {
                    continue;
                }
                for (JsonNode pricingOption : pricingOptions) {
                    this.addPrice(prices, pricingOption.path("price"));
                    JsonNode items = pricingOption.path("items");
                    if (!items.isArray()) {
                        continue;
                    }
                    for (JsonNode item : items) {
                        this.addPrice(prices, item.path("price"));
                    }
                }
            }

            if (prices.isEmpty()) {
                throw this.flightNotFound();
            }
            return prices;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | NumberFormatException e) {
            log.warn("FlightAPI 응답 파싱 오류", e);
            throw this.flightApiUnavailable();
        }
    }

    private void addPrice(List<BigDecimal> prices, JsonNode priceNode) {
        JsonNode amount = priceNode.path("amount");
        if (amount.isNumber() || amount.isTextual()) {
            BigDecimal price = new BigDecimal(amount.asText());
            if (price.signum() > 0) {
                prices.add(price);
            }
        }
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

    private BusinessException toApiException(
            final int status,
            final String body) {
        if (status == 429 || this.isLimitExceeded(body)) {
            return new BusinessException(
                    "항공권 API 호출 한도를 초과했습니다.",
                    HttpStatus.TOO_MANY_REQUESTS,
                    "TRAVEL_019");
        }
        if (status == 404 || status == 410) {
            return this.flightNotFound();
        }
        return this.flightApiUnavailable();
    }

    private boolean isLimitExceeded(final String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        final String normalized = body.toLowerCase(Locale.ROOT);
        return normalized.contains("quota")
                || normalized.contains("rate limit")
                || normalized.contains("request limit")
                || normalized.contains("maximum limit")
                || normalized.contains("credit");
    }

    private BusinessException flightApiUnavailable() {
        return new BusinessException(
                "항공권 가격 서비스를 이용할 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "TRAVEL_017");
    }

    private BusinessException flightNotFound() {
        return BusinessException.notFound(
                "조회 가능한 왕복 항공권이 없습니다.",
                "TRAVEL_018");
    }
}

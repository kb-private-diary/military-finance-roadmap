package org.scoula.travel.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.travel.dto.TravelPlaceResponseDTO;

/**
 * SerpApi Google Maps 관광지·맛집 검색 클라이언트.
 */
@Log4j2
@Component
public class SerpApiClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 20_000;
    private static final int MAX_RESULTS = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${serpapi.api-key:}")
    private String apiKey;

    @Value("${serpapi.api-url:https://serpapi.com/search.json}")
    private String apiUrl;

    public List<TravelPlaceResponseDTO> searchPlaces(
            final String country,
            final String city,
            final String category) {
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw this.serpApiUnavailable();
        }

        final String query = "restaurant".equals(category)
                ? String.join(" ", country, city, "맛집")
                : String.format(
                        "tourist attractions in %s, %s",
                        city,
                        country);
        final String requestUrl = this.apiUrl
                + "?engine=google_maps"
                + "&type=search"
                + "&hl=ko"
                + "&gl=" + this.countryCode(country)
                + "&q=" + this.encode(query)
                + "&api_key=" + this.encode(this.apiKey);
        final String body = this.get(requestUrl);

        try {
            final JsonNode root = this.objectMapper.readTree(body);
            final String error = root.path("error").asText();
            if (!error.isEmpty()) {
                log.warn("SerpApi 응답 오류: {}", error);
                if (this.isLimitError(error)) {
                    throw this.serpApiLimitExceeded();
                }
                throw this.serpApiUnavailable();
            }

            final JsonNode localResults = root.path("local_results");
            if (!localResults.isArray()) {
                final JsonNode placeResult = root.path("place_results");
                if (placeResult.isObject()
                        && !placeResult.path("title").asText().isEmpty()) {
                    return List.of(this.toResponse(placeResult));
                }
                return List.of();
            }

            final List<TravelPlaceResponseDTO> places = new ArrayList<>();
            for (final JsonNode result : localResults) {
                if (places.size() >= MAX_RESULTS) {
                    break;
                }
                final String title = result.path("title").asText();
                if (title.isEmpty()) {
                    continue;
                }
                places.add(this.toResponse(result));
            }
            return places;
        } catch (final BusinessException e) {
            throw e;
        } catch (final IOException e) {
            log.warn("SerpApi 응답 파싱 오류", e);
            throw this.serpApiUnavailable();
        }
    }

    private TravelPlaceResponseDTO toResponse(final JsonNode result) {
        final JsonNode coordinates = result.path("gps_coordinates");
        return TravelPlaceResponseDTO.builder()
                .placeId(result.path("place_id").asText(null))
                .title(result.path("title").asText())
                .type(result.path("type").asText(null))
                .address(result.path("address").asText(null))
                .rating(this.doubleValue(result.path("rating")))
                .reviews(this.integerValue(result.path("reviews")))
                .price(result.path("price").asText(null))
                .thumbnail(result.path("thumbnail").asText(null))
                .latitude(this.doubleValue(coordinates.path("latitude")))
                .longitude(this.doubleValue(coordinates.path("longitude")))
                .build();
    }

    private String get(final String requestUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(requestUrl)
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);

            final int status = connection.getResponseCode();
            final String body = this.readBody(
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream());
            if (status == 429) {
                throw this.serpApiLimitExceeded();
            }
            if (status < 200 || status >= 300) {
                log.warn(
                        "SerpApi 호출 실패: status={}, body={}",
                        status,
                        body);
                throw this.serpApiUnavailable();
            }
            return body;
        } catch (final BusinessException e) {
            throw e;
        } catch (final IOException e) {
            log.warn("SerpApi 통신 오류", e);
            throw this.serpApiUnavailable();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String countryCode(final String country) {
        if ("대한민국".equals(country)) {
            return "kr";
        }
        if ("일본".equals(country)) {
            return "jp";
        }
        if ("베트남".equals(country)) {
            return "vn";
        }
        return "kr";
    }

    private boolean isLimitError(final String error) {
        final String normalized = error.toLowerCase(Locale.ROOT);
        return normalized.contains("limit")
                || normalized.contains("credit")
                || normalized.contains("quota");
    }

    private Double doubleValue(final JsonNode value) {
        return value.isNumber() ? value.asDouble() : null;
    }

    private Integer integerValue(final JsonNode value) {
        return value.isNumber() ? value.asInt() : null;
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String readBody(final InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            final StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    private BusinessException serpApiLimitExceeded() {
        return new BusinessException(
                "관광지 검색 API의 호출 한도를 초과했습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "TRAVEL_023");
    }

    private BusinessException serpApiUnavailable() {
        return new BusinessException(
                "관광지 검색 서비스를 이용할 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "TRAVEL_024");
    }
}

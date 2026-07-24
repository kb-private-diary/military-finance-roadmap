package org.scoula.travel.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.travel.service.DomesticCityCoordinates.Coordinate;

/**
 * ODsay 대중교통 길찾기 v1.8 클라이언트.
 *
 * <p>서버 플랫폼 키를 백엔드에서만 사용하며 추천 경로의 편도 총 요금을 반환한다.</p>
 */
@Log4j2
@Component
public class OdsayClient {

    private static final String API_URL =
            "https://api.odsay.com/v1/api/searchPubTransPathT";
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 신규 표기는 소문자를 권장하지만 기존 ODsay.api-key 설정도 함께 지원한다.
    @Value("${odsay.api-key:${ODsay.api-key:}}")
    private String apiKey;

    public long estimateRoundTripCost(String departure, String destination) {
        if (departure == null || destination == null) {
            throw BusinessException.badRequest(
                    "출발지와 도착지가 필요합니다.", "TRAVEL_012");
        }
        if (departure.equals(destination)) {
            throw BusinessException.badRequest(
                    "출발지와 도착지는 달라야 합니다.", "TRAVEL_013");
        }
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new BusinessException(
                    "ODsay API 키가 설정되지 않았습니다.",
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "TRAVEL_014");
        }

        Coordinate start = DomesticCityCoordinates.get(departure);
        Coordinate end = DomesticCityCoordinates.get(destination);
        long oneWayCost = this.fetchRecommendedOneWayCost(start, end);

        try {
            return Math.multiplyExact(oneWayCost, 2L);
        } catch (ArithmeticException e) {
            throw new BusinessException(
                    "ODsay 교통비 계산 범위를 초과했습니다.",
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "TRAVEL_018");
        }
    }

    private long fetchRecommendedOneWayCost(Coordinate start, Coordinate end) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.buildUrl(start, end));
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
                log.warn("ODsay 호출 실패: status={}, body={}", status, body);
                throw this.externalApiException("ODsay API 호출에 실패했습니다.");
            }
            return this.extractRecommendedPayment(body);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.warn("ODsay 통신 오류", e);
            throw this.externalApiException("ODsay API에 연결할 수 없습니다.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildUrl(Coordinate start, Coordinate end) {
        return API_URL
                + "?SX=" + start.getLongitude()
                + "&SY=" + start.getLatitude()
                + "&EX=" + end.getLongitude()
                + "&EY=" + end.getLatitude()
                + "&OPT=0"
                + "&apiKey=" + URLEncoder.encode(
                        this.apiKey, StandardCharsets.UTF_8);
    }

    private long extractRecommendedPayment(String body) throws IOException {
        JsonNode root = this.objectMapper.readTree(body);
        JsonNode error = root.path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            String message = error.path("msg").asText("경로를 검색할 수 없습니다.");
            log.warn("ODsay 응답 오류: {}", message);
            throw this.externalApiException("ODsay 경로 검색에 실패했습니다: " + message);
        }

        JsonNode paths = root.path("result").path("path");
        if (!paths.isArray() || paths.size() == 0) {
            throw this.externalApiException("이동 가능한 대중교통 경로가 없습니다.");
        }

        // OPT=0은 추천순이므로 첫 번째 유효 경로의 총 요금을 사용한다.
        for (JsonNode path : paths) {
            JsonNode info = path.path("info");
            long payment = info.path("totalPayment").asLong(0L);
            if (payment <= 0L) {
                payment = info.path("payment").asLong(0L);
            }
            if (payment > 0L) {
                return payment;
            }
        }

        throw this.externalApiException("ODsay 경로 응답에서 교통비를 찾을 수 없습니다.");
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

    private BusinessException externalApiException(String message) {
        return new BusinessException(
                message,
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                "TRAVEL_015");
    }
}

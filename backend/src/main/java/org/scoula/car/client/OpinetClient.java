package org.scoula.car.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;

// 오피넷 전국 평균 유가정보 API 클라이언트
@Log4j2
@Component
public class OpinetClient {

    private static final String BASE_URL = "http://www.opinet.co.kr/api/avgAllPrice.do";
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 20_000;

    // 유종 코드 (오피넷 PRODCD 기준)
    public static final String PRODCD_GASOLINE = "B027"; // 휘발유
    public static final String PRODCD_DIESEL = "D047";   // 경유
    public static final String PRODCD_LPG = "K015";       // LPG

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${opinet.api-key}")
    private String apiKey;

    // 유종별 전국 평균 리터당 가격(원) 조회
    public BigDecimal fetchAvgPricePerLiter(String prodCode) {
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw this.opinetApiUnavailable();
        }

        HttpURLConnection connection = null;
        try {
            String query = "?code=" + this.encode(this.apiKey) + "&out=json";
            URL url = new URL(BASE_URL + query);
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
                log.warn("오피넷 API 호출 실패: status={}, body={}", status, body);
                throw this.opinetApiUnavailable();
            }

            JsonNode oilList = this.objectMapper.readTree(body).path("RESULT").path("OIL");
            for (JsonNode oil : oilList) {
                if (prodCode.equals(oil.path("PRODCD").asText())) {
                    return new BigDecimal(oil.path("PRICE").asText());
                }
            }
            log.warn("오피넷 응답에서 유종 코드 {}를 찾지 못함: body={}", prodCode, body);
            throw this.opinetApiUnavailable();
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | NumberFormatException e) {
            log.warn("오피넷 API 통신 오류", e);
            throw this.opinetApiUnavailable();
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

    private BusinessException opinetApiUnavailable() {
        return new BusinessException(
                "유가 정보 서비스를 이용할 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "CAR_002");
    }
}

package org.scoula.rent.client;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 로컬 API 주소→좌표(위경도) 변환 클라이언트
 * 국토부 매물엔 좌표가 없어 주소로 위경도를 얻어 SCHOOL 반경검색(ST_Distance_Sphere)에 사용
 */
@Slf4j
@Component
public class KakaoGeocodingClient {

    // 카카오 REST API 키 (application-secret.properties, 없으면 빈값)
    @Value("${kakao.rest-api-key:}")
    private String apiKey;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // 카카오 로컬 - 주소 검색 API
    private static final String ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    /**
     * 주소 문자열 → 좌표 [위도, 경도]
     * 못 찾거나 실패하면 null 반환 (매물은 좌표 없이 저장, 외부 API 실패가 매물 유실로 이어지지 않게)
     */
    public BigDecimal[] geocode(String address) {
        if (address == null || address.isBlank() || apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(ADDRESS_URL)
                    .queryParam("query", address)
                    .queryParam("size", 1) // 첫 후보만 필요
                    .build()
                    .encode(StandardCharsets.UTF_8) // 한글 주소 URL 인코딩
                    .toUri();

            // 카카오 인증 헤더 (KakaoAK {REST_API_KEY})
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);

            ResponseEntity<String> res = rest.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            // documents 배열 첫 건의 x(경도)·y(위도) 사용
            JsonNode docs = om.readTree(res.getBody()).path("documents");
            if (!docs.isArray() || docs.isEmpty()) {
                return null; // 검색 결과 없음
            }
            JsonNode first = docs.get(0);
            BigDecimal lng = new BigDecimal(first.path("x").asText()); // 경도
            BigDecimal lat = new BigDecimal(first.path("y").asText()); // 위도
            return new BigDecimal[]{lat, lng};

        } catch (Exception e) {
            log.warn("카카오 지오코딩 실패 (address={}): {}", address, e.getMessage());
            return null;
        }
    }
}

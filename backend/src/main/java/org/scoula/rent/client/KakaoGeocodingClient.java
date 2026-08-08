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
    // 카카오 로컬 - 좌표→행정구역 변환 API (학교 좌표로 학교가 속한 시군구 판정)
    private static final String COORD2REGION_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

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

    /**
     * 좌표(위경도) → 시군구코드 5자리 (학교 좌표로 학교가 속한 시군구 판정용)
     * SCHOOL 모드에서 매물 좌표가 없어도 학교 지역 매물을 조회하기 위해 사용.
     * 못 찾거나 실패하면 null (호출부에서 기존 좌표 반경검색으로 폴백)
     */
    public String coord2sigungu(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null || apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(COORD2REGION_URL)
                    .queryParam("x", lng.toPlainString()) // 경도
                    .queryParam("y", lat.toPlainString()) // 위도
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);

            ResponseEntity<String> res = rest.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode docs = om.readTree(res.getBody()).path("documents");
            if (!docs.isArray() || docs.isEmpty()) {
                return null;
            }
            // region_type 'B'(법정동) 우선, code 앞 5자리 = 시군구코드 (매물 sigungu_code 와 매칭)
            for (JsonNode d : docs) {
                if ("B".equals(d.path("region_type").asText())) {
                    String code = d.path("code").asText();
                    if (code != null && code.length() >= 5) {
                        return code.substring(0, 5);
                    }
                }
            }
            String code = docs.get(0).path("code").asText();
            return (code != null && code.length() >= 5) ? code.substring(0, 5) : null;

        } catch (Exception e) {
            log.warn("카카오 좌표→시군구 실패 (lat={}, lng={}): {}", lat, lng, e.getMessage());
            return null;
        }
    }
}

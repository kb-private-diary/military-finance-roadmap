package org.scoula.rent.client;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import org.scoula.rent.dto.NearbyFacilityDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 로컬 카테고리 검색 클라이언트 (Step5 주변 편의시설)
 * 매물 좌표를 중심으로 지하철·편의점·병원·마트 최근접 1건씩 조회해 도보 분으로 환산한다
 */
@Slf4j
@Component
public class KakaoLocalClient {

    // 카카오 REST API 키 (지오코딩과 동일 키, application-secret.properties)
    @Value("${kakao.rest-api-key:}")
    private String apiKey;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // 카카오 로컬 - 카테고리로 장소 검색 API
    private static final String CATEGORY_URL = "https://dapi.kakao.com/v2/local/search/category.json";
    private static final int RADIUS_M = 1000;       // 반경 1km
    private static final int WALK_M_PER_MIN = 67;   // 도보 분속 약 67m (시속 4km 기준)

    // 카카오 카테고리 그룹코드 → 우리 표시 타입 (순서 유지: 지하철·편의점·병원·마트)
    // 버스정류장(BS)은 카카오 category_group에 없어 제외 (명세상 별도 API)
    private static final Map<String, String> CATEGORIES = new java.util.LinkedHashMap<>() {{
        put("SW8", "SUBWAY");     // 지하철역
        put("CS2", "CONVENIENCE"); // 편의점
        put("HP8", "HOSPITAL");    // 병원
        put("MT1", "MART");        // 대형마트
    }};

    /**
     * 매물 좌표 주변 편의시설 목록 (카테고리별 최근접 1건)
     * 좌표·키 없거나 검색 실패 시 빈 목록 (편의시설 조회 실패가 Step5 조회 자체를 막지 않게)
     */
    public List<NearbyFacilityDTO> searchNearby(BigDecimal latitude, BigDecimal longitude) {
        List<NearbyFacilityDTO> result = new ArrayList<>();
        if (latitude == null || longitude == null || apiKey == null || apiKey.isBlank()) {
            return result;
        }
        for (Map.Entry<String, String> entry : CATEGORIES.entrySet()) {
            NearbyFacilityDTO facility = searchNearest(entry.getKey(), entry.getValue(), latitude, longitude);
            if (facility != null) {
                result.add(facility);
            }
        }
        return result;
    }

    /** 특정 카테고리에서 가장 가까운 1건 (sort=distance) */
    private NearbyFacilityDTO searchNearest(String categoryCode, String type,
                                            BigDecimal latitude, BigDecimal longitude) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(CATEGORY_URL)
                    .queryParam("category_group_code", categoryCode)
                    .queryParam("x", longitude.toPlainString()) // 경도
                    .queryParam("y", latitude.toPlainString())  // 위도
                    .queryParam("radius", RADIUS_M)
                    .queryParam("sort", "distance")             // 가까운 순
                    .queryParam("size", 1)                      // 최근접 1건만
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);

            ResponseEntity<String> res = rest.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode docs = om.readTree(res.getBody()).path("documents");
            if (!docs.isArray() || docs.isEmpty()) {
                return null; // 반경 내 없음
            }
            JsonNode first = docs.get(0);
            String name = first.path("place_name").asText();
            int distanceM = first.path("distance").asInt(0); // 중심좌표로부터 직선거리(m)
            return new NearbyFacilityDTO(type, name, toWalkMinutes(distanceM));

        } catch (Exception e) {
            log.warn("카카오 로컬 검색 실패 (category={}): {}", categoryCode, e.getMessage());
            return null;
        }
    }

    /** 직선거리(m) → 도보 분 (올림, 최소 1분) */
    private int toWalkMinutes(int distanceM) {
        return Math.max(1, (int) Math.ceil((double) distanceM / WALK_M_PER_MIN));
    }
}

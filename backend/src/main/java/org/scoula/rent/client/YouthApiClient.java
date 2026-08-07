package org.scoula.rent.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.scoula.rent.dto.YouthPolicyDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 온통청년 OPEN API(getPlcy) 클라이언트. (연동명세 §1, §4-1)
 *
 * <p>{@code GET https://www.youthcenter.go.kr/go/ythip/getPlcy} 를 lclsfNm=주거 로 호출하고
 * 전체 페이지를 순회해 주거 정책 전량(약 281건)을 {@link YouthPolicyDTO} 목록으로 반환한다.
 * mclsfNm 파라미터는 pageSize 에 따라 결과가 달라지는 버그가 있어 사용하지 않고(연동명세 §1-4),
 * 중분류 필터는 SyncService(애플리케이션)에서 처리한다.</p>
 *
 * <p>외부 API 호출이므로 트랜잭션 밖에서 실행한다(HTTP I/O 를 트랜잭션에 넣지 않는다).
 * 개별 페이지 실패는 로그를 남기고 순회를 중단하되, 그때까지 받은 목록은 반환한다.</p>
 */
@Slf4j
@Component
public class YouthApiClient {

    // 공통 설정은 application.properties(youth.base-url), 키는 application-secret.properties(youth.api-key)
    @Value("${youth.base-url:https://www.youthcenter.go.kr/go/ythip/getPlcy}")
    private String baseUrl;

    @Value("${youth.api-key:}")
    private String apiKey;

    @Value("${youth.page-size:100}")
    private int pageSize;

    @Value("${youth.lclsf-nm:주거}")
    private String lclsfNm;

    @Value("${youth.max-page:50}")
    private int maxPage; // 무한루프 방지 상한

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 주거 정책 전량 수신 (연동명세 §4-1).
     * 키가 없거나 응답이 비정상이면 그때까지 모은 목록을 반환한다(빈 목록 가능).
     */
    public List<YouthPolicyDTO> fetchAllHousingPolicies() {
        List<YouthPolicyDTO> all = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("온통청년 API 키(youth.api-key)가 없어 동기화를 건너뜁니다.");
            return all;
        }

        int page = 1;
        int total = Integer.MAX_VALUE;
        while ((long) (page - 1) * pageSize < total) {
            JsonNode result = fetchPage(page);
            if (result == null) {
                break; // 페이지 실패 → 여기까지 받은 것만 반환
            }
            total = result.path("pagging").path("totCount").asInt(all.size());

            JsonNode list = result.path("youthPolicyList");
            if (list.isArray()) {
                for (JsonNode node : list) {
                    all.add(toDto(node));
                }
            }
            if (!list.isArray() || list.isEmpty()) {
                break; // 더 받을 게 없음
            }
            page++;
            if (page > maxPage) {
                log.warn("온통청년 API 페이지 상한({}) 도달로 순회 중단", maxPage);
                break;
            }
        }
        log.info("온통청년 주거 정책 수신 완료: {}건 (totCount={})", all.size(), total);
        return all;
    }

    /** 한 페이지 호출 → result 노드. 실패 시 null */
    private JsonNode fetchPage(int page) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("apiKeyNm", apiKey)
                    .queryParam("pageNum", page)
                    .queryParam("pageSize", pageSize)
                    .queryParam("lclsfNm", lclsfNm) // mclsfNm 은 사용하지 않는다(연동명세 §1-4)
                    .build()
                    .encode()
                    .toUri();

            String body = rest.getForObject(uri, String.class);
            if (body == null) {
                return null;
            }
            JsonNode root = om.readTree(body);
            int code = root.path("resultCode").asInt(0);
            if (code != 200) {
                log.warn("온통청년 API 비정상 응답 (page={}, resultCode={}, msg={})",
                        page, code, root.path("resultMessage").asText(""));
                return null;
            }
            return root.path("result");
        } catch (Exception e) {
            log.warn("온통청년 API 호출 실패 (page={}): {}", page, e.getMessage());
            return null;
        }
    }

    /** JSON 한 건 → DTO (필드 없으면 null/빈문자). KakaoLocalClient 의 JsonNode 패턴 참고 */
    private YouthPolicyDTO toDto(JsonNode n) {
        YouthPolicyDTO d = new YouthPolicyDTO();
        d.setPlcyNo(text(n, "plcyNo"));
        d.setPlcyNm(text(n, "plcyNm"));
        d.setPlcyExplnCn(text(n, "plcyExplnCn"));
        d.setPlcySprtCn(text(n, "plcySprtCn"));
        d.setLclsfNm(text(n, "lclsfNm"));
        d.setMclsfNm(text(n, "mclsfNm"));
        d.setPlcyKywdNm(text(n, "plcyKywdNm"));
        d.setPvsnInstGroupCd(text(n, "pvsnInstGroupCd"));
        d.setPlcyPvsnMthdCd(text(n, "plcyPvsnMthdCd"));
        d.setSprvsnInstCdNm(text(n, "sprvsnInstCdNm"));
        d.setRgtrHghrkInstCdNm(text(n, "rgtrHghrkInstCdNm"));
        d.setSprtTrgtMinAge(text(n, "sprtTrgtMinAge"));
        d.setSprtTrgtMaxAge(text(n, "sprtTrgtMaxAge"));
        d.setSprtTrgtAgeLmtYn(text(n, "sprtTrgtAgeLmtYn"));
        d.setEarnCndSeCd(text(n, "earnCndSeCd"));
        d.setEarnMaxAmt(text(n, "earnMaxAmt"));
        d.setEarnEtcCn(text(n, "earnEtcCn"));
        d.setAddAplyQlfcCndCn(text(n, "addAplyQlfcCndCn"));
        d.setPtcpPrpTrgtCn(text(n, "ptcpPrpTrgtCn"));
        d.setAplyPrdSeCd(text(n, "aplyPrdSeCd"));
        d.setAplyYmd(text(n, "aplyYmd"));
        d.setBizPrdBgngYmd(text(n, "bizPrdBgngYmd"));
        d.setBizPrdEndYmd(text(n, "bizPrdEndYmd"));
        d.setBizPrdEtcCn(text(n, "bizPrdEtcCn"));
        d.setPlcyAplyMthdCn(text(n, "plcyAplyMthdCn"));
        d.setAplyUrlAddr(text(n, "aplyUrlAddr"));
        d.setRefUrlAddr1(text(n, "refUrlAddr1"));
        d.setZipCd(text(n, "zipCd"));
        d.setSprtSclCnt(text(n, "sprtSclCnt"));
        d.setLastMdfcnDt(text(n, "lastMdfcnDt"));
        return d;
    }

    /** 노드에서 문자열 안전 추출 (없거나 null 이면 null) */
    private String text(JsonNode n, String field) {
        JsonNode v = n.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

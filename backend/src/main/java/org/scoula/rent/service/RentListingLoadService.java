package org.scoula.rent.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.scoula.rent.client.KakaoGeocodingClient;
import org.scoula.rent.domain.RentListingVO;
import org.scoula.rent.mapper.RentListingMapper;
import org.scoula.rent.mapper.RentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 국토교통부 전월세 실거래가 매물 적재 서비스 (개발용)
 * 오피스텔·연립다세대 API를 직접 호출해 JSON 파싱 후 rent_listing에 적재
 * (프로파일 무관 - dev에서도 실데이터 적재 가능, Mock/Real Client 이원화와 별개)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentListingLoadService {

    @Value("${molit.service-key:}")
    private String serviceKey;

    private final RentListingMapper listingMapper;
    private final RentMapper rentMapper;
    private final KakaoGeocodingClient geocodingClient; // 주소→좌표 (SCHOOL 반경검색용)
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // 국토부 실거래가 API (오피스텔 / 연립다세대)
    private static final String OFFI_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent";
    private static final String VILLA_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcRHRent/getRTMSDataSvcRHRent";
    private static final int ROWS_PER_PAGE = 100; // 국토부 API 페이지당 건수
    private static final int MAX_PAGES = 50;       // 시군구·월당 최대 페이지 (100×50=5000건, 무한루프 안전장치)

    /**
     * 대상 시군구들의 오피스텔·연립다세대 매물 적재
     * @param sigunguCodes 시군구코드 5자리 목록
     * @param dealYm 계약년월 (yyyyMM)
     * @return 적재한 매물 건수
     */
    public int load(List<String> sigunguCodes, String dealYm) {
        return load(sigunguCodes, dealYm, true); // 기본 좌표변환 포함 (SCHOOL 반경검색용)
    }

    /**
     * 매물 적재 (좌표변환 옵션)
     * @param withGeocoding false면 카카오 지오코딩 skip (전국 REGION 검색용, 일일한도 회피)
     */
    public int load(List<String> sigunguCodes, String dealYm, boolean withGeocoding) {
        int total = 0;
        for (String sigunguCode : sigunguCodes) {
            total += fetchAndSave(OFFI_URL, "OFFICETEL", "offiNm", sigunguCode, dealYm, withGeocoding);
            total += fetchAndSave(VILLA_URL, "VILLA", "mhouseNm", sigunguCode, dealYm, withGeocoding);
        }
        return total;
    }

    /** 전국 매물 적재 (좌표변환 없이 - REGION 검색용, 카카오 일일한도 회피) */
    public int loadNationwide(String dealYm) {
        return load(rentMapper.findAllSigunguCodes(), dealYm, false);
    }

    /** 한 API·시군구·월의 매물을 조회해 적재 (전체 페이지 순회, 한 건 실패해도 나머지는 계속) */
    private int fetchAndSave(String url, String estateType, String nameField,
                             String sigunguCode, String dealYm, boolean withGeocoding) {
        int saved = 0;
        try {
            // 멱등화: 같은 시군구·종류 기존 매물을 먼저 지우고 재삽입 → 재적재해도 중복이 누적되지 않는다.
            //   (확정 매물은 매퍼에서 제외해 보존. base_date 가 실행일이라 예전엔 재적재분이 통째로 중복 적재됐음)
            listingMapper.deleteByScope(estateType, sigunguCode);
            // 매물 많은 시군구는 100건을 넘으므로 totalCount를 볼 때까지 페이지를 순회 (안전장치 MAX_PAGES)
            for (int pageNo = 1; pageNo <= MAX_PAGES; pageNo++) {
                URI uri = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("LAWD_CD", sigunguCode)
                        .queryParam("DEAL_YMD", dealYm)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", ROWS_PER_PAGE)
                        .queryParam("_type", "json") // JSON 응답 명시
                        .build(true) // serviceKey가 이미 인코딩된 값이면 이중인코딩 방지
                        .toUri();

                byte[] body = rest.getForObject(uri, byte[].class);
                if (body == null || body.length == 0) {
                    break;
                }
                String json = decodeBody(body); // 서버가 gzip 압축으로 주면 해제

                // response.body.items.item = 매물 배열 (1건이면 객체, 0건이면 items가 빈 문자열)
                JsonNode bodyNode = om.readTree(json).path("response").path("body");
                List<JsonNode> items = toList(bodyNode.path("items").path("item"));
                if (items.isEmpty()) {
                    break; // 이 페이지에 매물 없음 = 끝
                }
                for (JsonNode item : items) {
                    long monthlyRent = won(text(item, "monthlyRent"));
                    if (monthlyRent <= 0) {
                        continue; // 전세(월세 0)는 제외 - 자취는 월세만
                    }
                    listingMapper.insertListing(toListing(item, estateType, nameField, sigunguCode, withGeocoding));
                    saved++;
                }
                // 누적 조회 건수가 전체 건수 이상이면 마지막 페이지 (전세 포함 totalCount 기준)
                int totalCount = bodyNode.path("totalCount").asInt(0);
                if ((long) pageNo * ROWS_PER_PAGE >= totalCount) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("매물 적재 실패 (type={}, sigungu={}, ym={}): {}", estateType, sigunguCode, dealYm, e.getMessage());
        }
        return saved;
    }

    /** item 노드 → 리스트 (배열이면 그대로 / 단일 객체면 [하나] / 없으면 빈) */
    private List<JsonNode> toList(JsonNode items) {
        List<JsonNode> list = new ArrayList<>();
        if (items.isArray()) {
            items.forEach(list::add);
        } else if (items.isObject()) {
            list.add(items);
        }
        return list;
    }

    /** item 한 건 → RentListingVO (만원→원 변환, 법정동코드 매핑) */
    private RentListingVO toListing(JsonNode item, String estateType, String nameField,
                                    String sigunguCode, boolean withGeocoding) {
        String umdName = text(item, "umdNm");

        RentListingVO vo = new RentListingVO();
        vo.setEstateType(estateType);
        vo.setSigunguCode(sigunguCode);
        vo.setRegionCode(rentMapper.findRegionCodeBySigunguAndUmd(sigunguCode, umdName)); // 코드 매핑 (없으면 null)
        vo.setUmdName(umdName);
        String jibun = text(item, "jibun");
        vo.setJibun(jibun);

        // 카카오 지오코딩 (withGeocoding=true 일 때만 - SCHOOL 반경검색용, 전국 REGION은 skip해 한도 회피)
        // 시도명은 region_code에서 조회해 "시도 법정동명 지번" 주소로 조합 (전국 대응, 하드코딩 제거)
        if (withGeocoding) {
            String sidoName = rentMapper.findSidoNameBySigunguCode(sigunguCode);
            BigDecimal[] coords = geocodingClient.geocode(
                    (sidoName == null ? "" : sidoName) + " " + umdName + " " + (jibun == null ? "" : jibun));
            if (coords != null) {
                vo.setLatitude(coords[0]);  // 위도
                vo.setLongitude(coords[1]); // 경도
            }
        }
        vo.setBuildingName(text(item, nameField)); // 오피스텔=offiNm / 빌라=mhouseNm
        vo.setBuiltYear(intOf(text(item, "buildYear")));
        vo.setFloor(intOf(text(item, "floor")));
        vo.setAreaSqm(decimalOf(text(item, "excluUseAr")));
        vo.setDeposit(won(text(item, "deposit")));
        vo.setMonthlyRent(won(text(item, "monthlyRent")));
        vo.setDealDate(dealDate(item));
        vo.setBaseDate(LocalDate.now());
        vo.setCreatedNm("BATCH");
        return vo;
    }

    /** 계약 년·월·일 → LocalDate */
    private LocalDate dealDate(JsonNode item) {
        Integer y = intOf(text(item, "dealYear"));
        Integer m = intOf(text(item, "dealMonth"));
        Integer d = intOf(text(item, "dealDay"));
        if (y == null || m == null || d == null) {
            return null;
        }
        return LocalDate.of(y, m, d);
    }

    // ------------------------------------------------------------------
    //  파싱 헬퍼
    // ------------------------------------------------------------------

    /** 응답 바이트 → 문자열 (gzip 매직넘버 0x1f8b면 압축 해제) */
    private String decodeBody(byte[] body) throws Exception {
        if (body.length >= 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(body))) {
                return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    /** JSON 필드 텍스트 (없거나 공백이면 null 처리 준비) */
    private String text(JsonNode item, String name) {
        JsonNode node = item.path(name);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null ? null : value.trim();
    }

    /** 만원 단위 문자열("5,000") → 원 단위 long */
    private long won(String manwon) {
        if (manwon == null || manwon.isBlank()) {
            return 0;
        }
        return Long.parseLong(manwon.replace(",", "").trim()) * 10000L;
    }

    private Integer intOf(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return Integer.parseInt(s.replace(",", "").trim());
    }

    private BigDecimal decimalOf(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return new BigDecimal(s.trim());
    }
}

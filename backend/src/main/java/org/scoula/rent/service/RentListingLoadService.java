package org.scoula.rent.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // 국토부 실거래가 API (오피스텔 / 연립다세대)
    private static final String OFFI_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent";
    private static final String VILLA_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcRHRent/getRTMSDataSvcRHRent";

    /**
     * 대상 시군구들의 오피스텔·연립다세대 매물 적재
     * @param sigunguCodes 시군구코드 5자리 목록
     * @param dealYm 계약년월 (yyyyMM)
     * @return 적재한 매물 건수
     */
    public int load(List<String> sigunguCodes, String dealYm) {
        int total = 0;
        for (String sigunguCode : sigunguCodes) {
            total += fetchAndSave(OFFI_URL, "OFFICETEL", "offiNm", sigunguCode, dealYm);
            total += fetchAndSave(VILLA_URL, "VILLA", "mhouseNm", sigunguCode, dealYm);
        }
        return total;
    }

    /** 한 API·시군구·월의 매물을 조회해 적재 (한 건 실패해도 나머지는 계속) */
    private int fetchAndSave(String url, String estateType, String nameField,
                             String sigunguCode, String dealYm) {
        int saved = 0;
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("LAWD_CD", sigunguCode)
                    .queryParam("DEAL_YMD", dealYm)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 100)
                    .queryParam("_type", "json") // JSON 응답 명시
                    .build(true) // serviceKey가 이미 인코딩된 값이면 이중인코딩 방지
                    .toUri();

            byte[] body = rest.getForObject(uri, byte[].class);
            if (body == null || body.length == 0) {
                return 0;
            }
            String json = decodeBody(body); // 서버가 gzip 압축으로 주면 해제

            // response.body.items.item = 매물 배열 (최상위에 response 래퍼 있음, 결과 1건이면 배열 아닌 객체, 0건이면 items가 빈 문자열)
            JsonNode items = om.readTree(json).path("response").path("body").path("items").path("item");
            for (JsonNode item : toList(items)) {
                long monthlyRent = won(text(item, "monthlyRent"));
                if (monthlyRent <= 0) {
                    continue; // 전세(월세 0)는 제외 - 자취는 월세만
                }
                listingMapper.insertListing(toListing(item, estateType, nameField, sigunguCode));
                saved++;
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
    private RentListingVO toListing(JsonNode item, String estateType, String nameField, String sigunguCode) {
        String umdName = text(item, "umdNm");

        RentListingVO vo = new RentListingVO();
        vo.setEstateType(estateType);
        vo.setSigunguCode(sigunguCode);
        vo.setRegionCode(rentMapper.findRegionCodeBySigunguAndUmd(sigunguCode, umdName)); // 코드 매핑 (없으면 null)
        vo.setUmdName(umdName);
        vo.setJibun(text(item, "jibun"));
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

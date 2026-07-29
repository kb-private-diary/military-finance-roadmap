package org.scoula.product.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.scoula.common.exception.BusinessException;
import org.scoula.product.domain.SavingProductVO;

/**
 * 금융감독원 "금융상품 한눈에"(finlife) API 클라이언트.
 *
 * <p>은행권(topFinGrpNo=020000) 예·적금 상품 중 KB국민은행 상품만 걸러서,
 * (fin_prdt_cd, save_trm) 조합마다 한 행씩 {@link SavingProductVO}로 변환해 반환한다.
 * FSS API에 없는 필드(min_limit, is_tax_exempt, gov_match_rate, product_link)는
 * 채우지 않고 null/기본값으로 남겨두며, 실제 값은 DB에 이미 저장된 값을 그대로 둔다
 * (ProductMapper.upsertSavingProduct 의 ON DUPLICATE KEY UPDATE 참고).</p>
 */
@Log4j2
@Component
public class FssApiClient {

    private static final String BASE_URL = "https://finlife.fss.or.kr/finlifeapi";
    private static final String BANK_GRP_NO = "020000";
    private static final String KB_CO_NM = "국민은행";
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 20_000;

    private static final String ENDPOINT_SAVING = "savingProductsSearch.json";
    private static final String ENDPOINT_DEPOSIT = "depositProductsSearch.json";
    private static final String PRODUCT_TYPE_SAVING = "SAVING";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fss.api-key}")
    private String apiKey;

    // productType: "SAVING"(적금) 또는 "DEPOSIT"(예금)
    public List<SavingProductVO> fetchKbProducts(String productType) {
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw this.fssApiUnavailable();
        }
        String endpoint = PRODUCT_TYPE_SAVING.equals(productType) ? ENDPOINT_SAVING : ENDPOINT_DEPOSIT;

        List<SavingProductVO> result = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            JsonNode page = this.fetchPage(endpoint, pageNo);
            this.collectKbRows(page, productType, result);

            int maxPageNo = page.path("max_page_no").asInt(pageNo);
            if (pageNo >= maxPageNo) {
                break;
            }
            pageNo++;
        }
        return result;
    }

    private void collectKbRows(JsonNode result, String productType, List<SavingProductVO> out) {
        JsonNode baseList = result.path("baseList");
        JsonNode optionList = result.path("optionList");

        for (JsonNode base : baseList) {
            if (!KB_CO_NM.equals(base.path("kor_co_nm").asText())) {
                continue;
            }
            String finPrdtCd = base.path("fin_prdt_cd").asText();
            boolean isActive = base.path("dcls_end_day").asText("").isEmpty();
            Long maxLimit = base.path("max_limit").isNumber() ? base.path("max_limit").asLong() : null;

            for (JsonNode option : optionList) {
                if (!finPrdtCd.equals(option.path("fin_prdt_cd").asText())) {
                    continue;
                }
                SavingProductVO vo = SavingProductVO.builder()
                        .productType(productType)
                        .isActive(isActive)
                        .finPrdtCd(finPrdtCd)
                        .korCoNm(base.path("kor_co_nm").asText())
                        .productName(base.path("fin_prdt_nm").asText())
                        .joinMember(base.path("join_member").asText(""))
                        .etcNote(base.path("etc_note").asText(""))
                        .maxLimit(maxLimit)
                        .intrRateType(option.path("intr_rate_type").asText())
                        // 예금 옵션엔 rsrv_type 이 없어서 기존 데이터 관례대로 NONE 으로 채운다
                        .rsrvType(option.path("rsrv_type").asText("NONE"))
                        .saveTrm(option.path("save_trm").asInt())
                        .spclCnd(base.path("spcl_cnd").asText(""))
                        .basicRate(this.toRate(option.path("intr_rate")))
                        .maxRate(this.toRate(option.path("intr_rate2")))
                        .build();
                out.add(vo);
            }
        }
    }

    private BigDecimal toRate(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(node.asText("0"));
        } catch (NumberFormatException e) {
            log.warn("FSS 응답 금리 파싱 실패: value={}", node.asText(), e);
            throw this.fssApiUnavailable();
        }
    }

    private JsonNode fetchPage(String endpoint, int pageNo) {
        HttpURLConnection connection = null;
        try {
            String query = "?auth=" + this.encode(this.apiKey)
                    + "&topFinGrpNo=" + BANK_GRP_NO
                    + "&pageNo=" + pageNo;
            URL url = new URL(BASE_URL + "/" + endpoint + query);
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
                log.warn("FSS API 호출 실패: status={}, body={}", status, body);
                throw this.fssApiUnavailable();
            }
            return this.objectMapper.readTree(body).path("result");
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.warn("FSS API 통신 오류", e);
            throw this.fssApiUnavailable();
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

    private BusinessException fssApiUnavailable() {
        return new BusinessException(
                "금융상품 정보 서비스를 이용할 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "PRODU_002");
    }
}

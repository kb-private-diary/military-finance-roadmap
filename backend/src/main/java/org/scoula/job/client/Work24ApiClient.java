package org.scoula.job.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class Work24ApiClient {

    // 훈련과정 목록 조회
    private static final String TRAINING_LIST_PATH =
            "/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do";

    // 훈련과정 상세 조회
    private static final String TRAINING_DETAIL_PATH =
            "/cm/openApi/call/hr/callOpenApiSvcInfo310L02.do";

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 20_000;

    private static final DateTimeFormatter REQUEST_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter RESPONSE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${work24.api-url}")
    private String apiUrl;

    @Value("${work24.auth-key}")
    private String authKey;


    /**
     * 고용24 훈련과정 목록 조회
     *
     * @param regionCode 시도 코드 ex) 서울 11
     * @param ncsCode NCS 4차 코드 ex) 20010202
     * @param courseType 훈련유형 ex) C0104(K-디지털), null이면 전체
     * @param ncsDepth 조회에 사용할 NCS 단계 (4=세분류, 3=소분류, 2=중분류)
     */
    public List<TrainingCourse> findTrainingCourses(
            String regionCode,
            String ncsCode,
            String courseType,
            int ncsDepth) {

        this.validateRegionCode(regionCode);
        this.validateNcsCode(ncsCode);

        // 실제 신청 준비기간을 고려하여
        // 조회일 기준 7일 이후 ~ 3개월 이내 개강 과정 조회
        LocalDate searchStartDate =
                LocalDate.now().plusDays(7);

        LocalDate searchEndDate =
                LocalDate.now().plusMonths(3);

        String startDate =
                searchStartDate.format(REQUEST_DATE_FORMAT);

        String endDate =
                searchEndDate.format(REQUEST_DATE_FORMAT);

        /*
         * NCS 코드는 단계마다 2자리씩 늘어나며 직종이 좁아진다.
         * 4차(8자리)로 결과가 없는 직종이 있어 요청받은 단계의 코드만 조건으로 붙인다.
         */
        String ncsSearchCode = ncsCode.substring(0, ncsDepth * 2);

        String path = TRAINING_LIST_PATH
                + "?authKey=" + this.encode(authKey)
                + "&returnType=XML"
                + "&outType=1"
                + "&pageNum=1"
                + "&pageSize=20"
                + "&srchTraStDt=" + startDate
                + "&srchTraEndDt=" + endDate
                + "&srchTraArea1=" + this.encode(regionCode)
                + "&srchNcs" + ncsDepth + "=" + this.encode(ncsSearchCode)
                + "&sort=DESC"
                + "&sortCol=5";

        // K-디지털 등 특정 훈련유형이 있는 경우에만 추가
        if (courseType != null && !courseType.isBlank()) {
            path += "&crseTracseSe=" + this.encode(courseType);
        }

        String body = this.get(path);

        try {
            Document document = this.parseXml(body);

            NodeList itemNodes =
                    document.getElementsByTagName("scn_list");

            List<TrainingCourse> courses =
                    new ArrayList<>();

            for (int i = 0; i < itemNodes.getLength(); i++) {

                Node node = itemNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element item = (Element) node;

                String trainingId =
                        this.getText(item, "trprId");

                Integer trainingRound =
                        this.parseInteger(
                                this.getText(item, "trprDegr")
                        );

                String institutionId =
                        this.getText(item, "trainstCstId");

                courses.add(
                        new TrainingCourse(
                                trainingId,
                                trainingRound,
                                institutionId,
                                this.getText(item, "title"),
                                this.getText(item, "subTitle"),
                                this.parseAmount(
                                        this.getText(item, "courseMan")
                                ),
                                this.getText(item, "address"),
                                this.parseResponseDate(
                                        this.getText(item, "traStartDate")
                                ),
                                this.parseResponseDate(
                                        this.getText(item, "traEndDate")
                                ),
                                this.getText(item, "titleLink"),
                                this.getText(item, "ncsCd"),
                                this.getText(item, "trainTarget"),
                                this.getText(item, "trainTargetCd")
                        )
                );
            }

            log.info(
                    "고용24 훈련과정 조회 완료: regionCode={}, ncsCode={}, ncsDepth={}, courseType={}, count={}",
                    regionCode,
                    ncsCode,
                    ncsDepth,
                    courseType,
                    courses.size()
            );

            return courses;

        } catch (Exception e) {

            log.warn(
                    "고용24 훈련과정 응답 파싱 오류: regionCode={}, ncsCode={}",
                    regionCode,
                    ncsCode,
                    e
            );

            throw this.work24ApiUnavailable();
        }
    }


    /**
     * 고용24 훈련과정 상세 조회
     * 일반훈련생 기준 본인부담액 조회
     *
     * XML:
     * <tgcrGnrlTrneOwepAllt>400000</tgcrGnrlTrneOwepAllt>
     */
    public Long findSelfPayment(
            String trainingId,
            Integer trainingRound,
            String institutionId) {

        if (trainingId == null || trainingId.isBlank()) {
            return null;
        }

        if (trainingRound == null) {
            return null;
        }

        if (institutionId == null || institutionId.isBlank()) {
            return null;
        }

        String path = TRAINING_DETAIL_PATH
                + "?authKey=" + this.encode(authKey)
                + "&returnType=XML"
                + "&outType=2"
                + "&srchTrprId=" + this.encode(trainingId)
                + "&srchTrprDegr=" + trainingRound
                + "&srchTorgId=" + this.encode(institutionId);

        String body = this.get(path);

        try {
            Document document = this.parseXml(body);

            NodeList nodes =
                    document.getElementsByTagName(
                            "tgcrGnrlTrneOwepAllt"
                    );

            if (nodes.getLength() == 0) {

                log.warn(
                        "고용24 본인부담액 없음: trainingId={}, trainingRound={}",
                        trainingId,
                        trainingRound
                );

                return null;
            }

            Long selfPayment =
                    this.parseAmount(
                            nodes.item(0).getTextContent()
                    );

            log.info(
                    "고용24 본인부담액 조회 완료: trainingId={}, trainingRound={}, selfPayment={}",
                    trainingId,
                    trainingRound,
                    selfPayment
            );

            return selfPayment;

        } catch (Exception e) {

            /*
             * 목록 조회 자체를 실패시키지 않기 위해
             * 상세 금액 조회 실패 시 null 반환
             */
            log.warn(
                    "고용24 본인부담액 조회 실패: trainingId={}, trainingRound={}",
                    trainingId,
                    trainingRound,
                    e
            );

            return null;
        }
    }


    /**
     * GET 요청
     */
    private String get(String path) {

        final int maxRetry = 3;
        final long retryDelayMillis = 1_000L;

        for (int attempt = 1;
             attempt <= maxRetry;
             attempt++) {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(this.apiUrl + path);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                connection.setRequestProperty(
                        "Accept",
                        "application/xml"
                );

                connection.setConnectTimeout(
                        CONNECT_TIMEOUT_MILLIS
                );

                connection.setReadTimeout(
                        READ_TIMEOUT_MILLIS
                );

                int status =
                        connection.getResponseCode();

                String body =
                        this.readBody(
                                status >= 200 && status < 300
                                        ? connection.getInputStream()
                                        : connection.getErrorStream()
                        );

                if (status >= 200 && status < 300) {
                    return body;
                }

                log.warn(
                        "고용24 API 호출 실패: attempt={}/{}, status={}, body={}",
                        attempt,
                        maxRetry,
                        status,
                        body
                );

            } catch (IOException e) {

                log.warn(
                        "고용24 API 통신 오류: attempt={}/{}",
                        attempt,
                        maxRetry,
                        e
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

            if (attempt < maxRetry) {

                try {

                    Thread.sleep(retryDelayMillis);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    break;
                }
            }
        }

        throw this.work24ApiUnavailable();
    }


    /**
     * XML 파싱
     */
    private Document parseXml(String xml)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        return builder.parse(
                new InputSource(
                        new StringReader(xml)
                )
        );
    }


    private String getText(
            Element element,
            String tagName) {

        NodeList nodes =
                element.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            return null;
        }

        String value =
                nodes.item(0).getTextContent();

        return value == null
                ? null
                : value.trim();
    }


    private LocalDate parseResponseDate(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            if (value.contains("-")) {

                return LocalDate.parse(
                        value,
                        RESPONSE_DATE_FORMAT
                );
            }

            return LocalDate.parse(
                    value,
                    REQUEST_DATE_FORMAT
            );

        } catch (DateTimeParseException e) {

            log.warn(
                    "고용24 날짜 형식 오류: value={}",
                    value
            );

            return null;
        }
    }


    private Long parseAmount(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return Long.valueOf(
                    value
                            .replace(",", "")
                            .trim()
            );

        } catch (NumberFormatException e) {

            log.warn(
                    "고용24 금액 변환 실패: value={}",
                    value
            );

            return null;
        }
    }


    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return Integer.valueOf(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            log.warn(
                    "고용24 숫자 변환 실패: value={}",
                    value
            );

            return null;
        }
    }


    private void validateRegionCode(
            String regionCode) {

        if (regionCode == null
                || regionCode.isBlank()) {

            throw BusinessException.badRequest(
                    "훈련지역을 선택해 주세요.",
                    "JOB_011"
            );
        }
    }


    private void validateNcsCode(
            String ncsCode) {

        if (ncsCode == null
                || ncsCode.length() != 8) {

            throw BusinessException.badRequest(
                    "NCS 직종코드가 올바르지 않습니다.",
                    "JOB_012"
            );
        }
    }


    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }


    private String readBody(InputStream stream)
            throws IOException {

        if (stream == null) {
            return "";
        }

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            StringBuilder body =
                    new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            return body.toString();
        }
    }


    private BusinessException work24ApiUnavailable() {

        return new BusinessException(
                "훈련과정 정보를 불러올 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "JOB_013"
        );
    }


    @Getter
    @AllArgsConstructor
    public static class TrainingCourse {

        // 고용24 훈련과정 ID
        private final String externalCode;

        // 훈련회차
        private final Integer trainingRound;

        // 훈련기관 ID - 상세 API 호출 시 필요
        private final String institutionId;

        // 훈련과정명
        private final String trainingName;

        // 훈련기관명
        private final String institutionName;

        // 전체 훈련비
        private final Long trainingCost;

        // 훈련지역
        private final String address;

        // 시작일
        private final LocalDate startDate;

        // 종료일
        private final LocalDate endDate;

        // 고용24 상세 URL
        private final String detailUrl;

        // NCS 코드
        private final String ncsCode;

        // 훈련유형명
        private final String trainingType;

        // 훈련유형코드
        private final String trainingTypeCode;
    }
}
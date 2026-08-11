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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Component
public class QnetApiClient {

    private static final String SCHEDULE_PATH = "/getJMList";
    private static final String FEE_PATH = "/getFeeList";

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 20_000;

    private static final DateTimeFormatter QNET_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    // ex) "1차 : 19400, 2차 : 22600"
    private static final Pattern FEE_PATTERN = Pattern.compile(
            "1차\\s*:\\s*([\\d,]+).*?2차\\s*:\\s*([\\d,]+)"
    );

    @Value("${qnet.api-url}")
    private String apiUrl;

    @Value("${qnet.service-key}")
    private String serviceKey;

    /**
     * Q-Net 자격증 응시료 조회
     *
     * @param jmCd Q-Net 종목코드
     * @return 필기·실기 응시료
     */
    public ExamFee findExamFee(String jmCd) {
        this.validateJmCd(jmCd);

        String path = FEE_PATH
                + "?serviceKey=" + this.encode(serviceKey)
                + "&jmCd=" + this.encode(jmCd);

        String body = this.get(path);

        try {
            Document document = this.parseXml(body);
            this.validateResponse(document);

            NodeList itemNodes = document.getElementsByTagName("item");

            if (itemNodes.getLength() == 0) {
                log.warn("Q-Net 응시료 조회 결과 없음: jmCd={}", jmCd);
                return new ExamFee(null, null);
            }

            Element item = (Element) itemNodes.item(0);

            String contents = this.getText(item, "contents");

            if (contents == null || contents.isBlank()) {
                log.warn("Q-Net 응시료 정보 없음: jmCd={}", jmCd);
                return new ExamFee(null, null);
            }

            Matcher matcher = FEE_PATTERN.matcher(contents);

            if (!matcher.find()) {
                log.warn(
                        "Q-Net 응시료 형식 파싱 실패: jmCd={}, contents={}",
                        jmCd,
                        contents
                );
                return new ExamFee(null, null);
            }

            Long writtenFee = this.parseAmount(matcher.group(1));
            Long practicalFee = this.parseAmount(matcher.group(2));

            return new ExamFee(writtenFee, practicalFee);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Q-Net 응시료 응답 파싱 오류: jmCd={}", jmCd, e);
            throw this.qnetApiUnavailable();
        }
    }

    /**
     * Q-Net 자격증 시험일정 목록 조회
     *
     * @param jmCd Q-Net 종목코드
     * @return 시험 회차별 일정 목록
     */
    public List<ExamSchedule> findExamSchedules(String jmCd) {
        this.validateJmCd(jmCd);

        String path = SCHEDULE_PATH
                + "?serviceKey=" + this.encode(serviceKey)
                + "&jmCd=" + this.encode(jmCd);

        String body = this.get(path);

        try {
            Document document = this.parseXml(body);
            this.validateResponse(document);

            NodeList itemNodes = document.getElementsByTagName("item");

            List<ExamSchedule> schedules = new ArrayList<>();

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node node = itemNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element item = (Element) node;

                schedules.add(
                        new ExamSchedule(
                                this.getText(item, "implplannm"),
                                this.getText(item, "jmfldnm"),

                                this.parseDate(this.getText(item, "docregstartdt")),
                                this.parseDate(this.getText(item, "docregenddt")),

                                this.parseDate(this.getText(item, "docexamstartdt")),
                                this.parseDate(this.getText(item, "docexamenddt")),

                                this.parseDate(this.getText(item, "docpassdt")),

                                this.parseDate(this.getText(item, "pracregstartdt")),
                                this.parseDate(this.getText(item, "pracregenddt")),

                                this.parseDate(this.getText(item, "pracexamstartdt")),
                                this.parseDate(this.getText(item, "pracexamenddt")),

                                this.parseDate(this.getText(item, "pracpassstartdt")),
                                this.parseDate(this.getText(item, "pracpassenddt"))
                        )
                );
            }

            return schedules;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Q-Net 시험일정 응답 파싱 오류: jmCd={}", jmCd, e);
            throw this.qnetApiUnavailable();
        }
    }

    /**
     * Q-Net GET 요청
     * 일시적인 통신 오류를 대비해 최대 3회까지 재시도
     */
    private String get(String path) {
        final int maxRetry = 3;
        final long retryDelayMillis = 1_000L;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(this.apiUrl + path);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/xml");
                connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
                connection.setReadTimeout(READ_TIMEOUT_MILLIS);

                int status = connection.getResponseCode();

                String body = this.readBody(
                        status >= 200 && status < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream()
                );

                if (status >= 200 && status < 300) {
                    return body;
                }

                log.warn(
                        "Q-Net API 호출 실패: attempt={}/{}, status={}, path={}, body={}",
                        attempt,
                        maxRetry,
                        status,
                        path,
                        body
                );

            } catch (IOException e) {

                log.warn(
                        "Q-Net API 통신 오류: attempt={}/{}, path={}",
                        attempt,
                        maxRetry,
                        path,
                        e
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            // 마지막 시도가 아니라면 잠시 대기 후 재시도
            if (attempt < maxRetry) {
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    log.warn(
                            "Q-Net API 재시도 대기 중 인터럽트 발생: path={}",
                            path
                    );

                    break;
                }
            }
        }

        throw this.qnetApiUnavailable();
    }

    /**
     * XML 문자열 파싱
     */
    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        // 외부 엔티티 비활성화
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

        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(
                new InputSource(new StringReader(xml))
        );
    }

    /**
     * Q-Net 응답 resultCode 검증
     */
    private void validateResponse(Document document) {
        NodeList resultCodeNodes =
                document.getElementsByTagName("resultCode");

        if (resultCodeNodes.getLength() == 0) {
            throw this.qnetApiUnavailable();
        }

        String resultCode =
                resultCodeNodes.item(0).getTextContent().trim();

        if (!"00".equals(resultCode)) {
            String resultMessage = "";

            NodeList resultMessageNodes =
                    document.getElementsByTagName("resultMsg");

            if (resultMessageNodes.getLength() > 0) {
                resultMessage =
                        resultMessageNodes.item(0)
                                .getTextContent()
                                .trim();
            }

            log.warn(
                    "Q-Net API 비정상 응답: resultCode={}, resultMsg={}",
                    resultCode,
                    resultMessage
            );

            throw this.qnetApiUnavailable();
        }
    }

    /**
     * XML item 내부 태그 값 조회
     */
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

    /**
     * yyyyMMdd → LocalDate
     */
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(
                    value,
                    QNET_DATE_FORMAT
            );
        } catch (DateTimeParseException e) {
            log.warn("Q-Net 날짜 형식 오류: value={}", value);
            return null;
        }
    }

    /**
     * 응시료 문자열 → 숫자
     */
    private Long parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(
                    value.replace(",", "").trim()
            );
        } catch (NumberFormatException e) {
            log.warn("Q-Net 응시료 숫자 변환 실패: value={}", value);
            return null;
        }
    }

    private void validateJmCd(String jmCd) {
        if (jmCd == null || jmCd.isBlank()) {
            throw BusinessException.badRequest(
                    "자격증 종목코드가 필요합니다.",
                    "JOB_009"
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

    private BusinessException qnetApiUnavailable() {
        return new BusinessException(
                "자격증 정보를 불러올 수 없습니다.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "JOB_010"
        );
    }

    /**
     * 응시료 조회 결과
     */
    @Getter
    @AllArgsConstructor
    public static class ExamFee {

        // 필기 응시료
        private final Long writtenFee;

        // 실기 응시료
        private final Long practicalFee;
    }

    /**
     * 시험일정 조회 결과
     */
    @Getter
    @AllArgsConstructor
    public static class ExamSchedule {

        // 시험 회차명
        private final String examRound;

        // 자격증명
        private final String qualificationName;

        // 필기 원서접수
        private final LocalDate writtenRegStartDate;
        private final LocalDate writtenRegEndDate;

        // 필기시험
        private final LocalDate writtenExamStartDate;
        private final LocalDate writtenExamEndDate;

        // 필기 합격발표
        private final LocalDate writtenResultDate;

        // 실기 원서접수
        private final LocalDate practicalRegStartDate;
        private final LocalDate practicalRegEndDate;

        // 실기시험
        private final LocalDate practicalExamStartDate;
        private final LocalDate practicalExamEndDate;

        // 최종 합격발표
        private final LocalDate practicalResultStartDate;
        private final LocalDate practicalResultEndDate;
    }
}

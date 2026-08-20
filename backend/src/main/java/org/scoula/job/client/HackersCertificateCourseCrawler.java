package org.scoula.job.client;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.scoula.job.domain.JobCourseVO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class HackersCertificateCourseCrawler {

    private static final String HACKERS_PASS_PAGE_URL =
            "https://pass.hackers.com/?r=pass&c=lecture";

    private static final String BASE_URL =
            "https://pass.hackers.com";

    private static final String COURSE_TYPE = "C01";
    private static final String PROVIDER_NAME = "해커스";

    private static final int WAIT_SECONDS = 10;
    private static final int MAX_COURSE_COUNT = 3;

    /**
     * 자격증 이름으로 해커스 강의를 조회한다.
     *
     * 예)
     * 산업안전기사
     * 정보처리기사
     * 전기기사
     */
    public List<JobCourseVO> crawlCertificateCourses(
            final String qualificationName
    ) {

        final ChromeOptions options =
                new ChromeOptions();

        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--no-sandbox",
                "--window-size=1920,1080"
        );

        final WebDriver driver =
                new ChromeDriver(options);

        try {

            final WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(WAIT_SECONDS)
                    );

            driver.get(
                    HACKERS_PASS_PAGE_URL
            );

            // 1. 우리 DB 자격증명과 동일한 해커스 항목 찾기
            final WebElement qualificationElement =
                    this.findQualificationElement(
                            driver,
                            wait,
                            qualificationName
                    );

            if (qualificationElement == null) {

                log.info(
                        "해커스에 해당 자격증이 없습니다. - qualificationName: {}",
                        qualificationName
                );

                return List.of();
            }

            final String categoryRoute =
                    qualificationElement.getAttribute(
                            "value"
                    );

            log.info(
                    "해커스 자격증 자동 탐색 완료 - qualificationName: {}, categoryRoute: {}",
                    qualificationName,
                    categoryRoute
            );

            // 2. 해당 자격증 선택
            this.clickElement(
                    driver,
                    qualificationElement
            );

            // 3. 조회 버튼 클릭
            final WebElement searchButton =
                    this.findSearchButton(
                            driver,
                            wait
                    );

            this.clickElement(
                    driver,
                    searchButton
            );

            // 4. 선택한 자격증명이 포함된 결과가 실제로 뜰 때까지 기다림
            final List<WebElement> courseElements =
                    this.waitForCourseResults(
                            driver,
                            wait,
                            qualificationName
                    );

            log.info(
                    "해커스 조회 결과 발견 - qualificationName: {}, elementCount: {}",
                    qualificationName,
                    courseElements.size()
            );

            // 5. 최대 3개를 JobCourseVO로 변환
            final List<JobCourseVO> courses =
                    this.parseCourses(
                            courseElements,
                            qualificationName
                    );

            log.info(
                    "해커스 자격증 강의 조회 완료 - qualificationName: {}, count: {}",
                    qualificationName,
                    courses.size()
            );

            return courses;

        } catch (TimeoutException e) {

            log.info(
                    "해커스 자격증 또는 강의 조회 시간 초과 - qualificationName: {}",
                    qualificationName
            );

            return List.of();

        } catch (Exception e) {

            log.warn(
                    "해커스 자격증 강의 크롤링 실패 - qualificationName: {}",
                    qualificationName,
                    e
            );

            return List.of();

        } finally {

            driver.quit();
        }
    }

    /**
     * 자격증명으로 해커스 Smart Finder의 항목을 찾는다.
     *
     * 예)
     * qualificationName = 산업안전기사
     *
     * data-categoryitemname="산업안전기사"
     */
    private WebElement findQualificationElement(
            final WebDriver driver,
            final WebDriverWait wait,
            final String qualificationName
    ) {

        try {

            return wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(
                                    "input[data-categoryitemname=\""
                                            + qualificationName
                                            + "\"]"
                            )
                    )
            );

        } catch (TimeoutException e) {

            return null;
        }
    }

    /**
     * 조회 버튼 탐색
     */
    private WebElement findSearchButton(
            final WebDriver driver,
            final WebDriverWait wait
    ) {

        try {

            return wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath(
                                    "//button[contains(normalize-space(.), '조회')]"
                            )
                    )
            );

        } catch (TimeoutException e) {

            return wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath(
                                    "//*[self::a or self::input]"
                                            + "[contains(@value, '조회') "
                                            + "or contains(normalize-space(.), '조회')]"
                            )
                    )
            );
        }
    }

    /**
     * JavaScript 클릭
     */
    private void clickElement(
            final WebDriver driver,
            final WebElement element
    ) {

        ((JavascriptExecutor) driver)
                .executeScript(
                        "arguments[0].click();",
                        element
                );
    }

    /**
     * 조회 후 선택한 자격증 관련 강의가 렌더링될 때까지 기다린다.
     */
    private List<WebElement> waitForCourseResults(
            final WebDriver driver,
            final WebDriverWait wait,
            final String qualificationName
    ) {

        final String qualificationXPath =
                this.toXPathLiteral(
                        qualificationName
                );

        final By courseLocator =
                By.xpath(
                        "//*[contains("
                                + "concat(' ', normalize-space(@class), ' '), "
                                + "' lecture-item '"
                                + ")]"
                                + "[contains(normalize-space(.), "
                                + qualificationXPath
                                + ")]"
                );

        wait.until(
                webDriver -> {

                    final List<WebElement> elements =
                            webDriver.findElements(
                                    courseLocator
                            );

                    return !elements.isEmpty();
                }
        );

        return driver.findElements(
                courseLocator
        );
    }

    /**
     * Selenium 결과 → JobCourseVO
     */
    private List<JobCourseVO> parseCourses(
            final List<WebElement> courseElements,
            final String qualificationName
    ) {

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final WebElement courseElement
                : courseElements) {

            if (courses.size()
                    >= MAX_COURSE_COUNT) {
                break;
            }

            final String courseName =
                    this.extractCourseName(
                            courseElement
                    );

            if (courseName == null
                    || courseName.isBlank()) {
                continue;
            }

            if (!this.isRelatedCourse(
                    courseName,
                    qualificationName
            )) {
                continue;
            }

            final Long originalPrice =
                    this.extractOriginalPrice(
                            courseElement
                    );

            final Long salePrice =
                    this.extractSalePrice(
                            courseElement
                    );

            if (originalPrice == null
                    && salePrice == null) {
                continue;
            }

            final String detailUrl =
                    this.extractDetailUrl(
                            courseElement
                    );

            final JobCourseVO course =
                    new JobCourseVO();

            course.setCourseType(
                    COURSE_TYPE
            );

            course.setProviderName(
                    PROVIDER_NAME
            );

            course.setCourseName(
                    courseName
            );

            course.setOriginalPrice(
                    originalPrice != null
                            ? originalPrice
                            : salePrice
            );

            course.setDiscountPrice(
                    this.normalizeDiscountPrice(
                            originalPrice,
                            salePrice
                    )
            );

            course.setMilitaryPrice(
                    null
            );

            course.setSelectedCost(
                    null
            );

            course.setDetailUrl(
                    detailUrl
            );

            courses.add(
                    course
            );
        }

        return courses;
    }

    /**
     * 강의명 추출
     */
    private String extractCourseName(
            final WebElement courseElement
    ) {

        final List<WebElement> nameElements =
                courseElement.findElements(
                        By.cssSelector(
                                ".lecture-name"
                        )
                );

        if (!nameElements.isEmpty()) {

            return nameElements
                    .get(0)
                    .getText()
                    .trim();
        }

        // lecture-name이 없을 경우 fallback
        final List<WebElement> titleElements =
                courseElement.findElements(
                        By.cssSelector(
                                ".lecture-title"
                        )
                );

        if (!titleElements.isEmpty()) {

            return titleElements
                    .get(0)
                    .getText()
                    .trim();
        }

        return null;
    }

    /**
     * 정가 추출
     */
    private Long extractOriginalPrice(
            final WebElement courseElement
    ) {

        final List<WebElement> elements =
                courseElement.findElements(
                        By.cssSelector(
                                ".lecture-price del"
                        )
                );

        if (elements.isEmpty()) {

            return this.extractSalePrice(
                    courseElement
            );
        }

        return this.parsePrice(
                elements
                        .get(0)
                        .getText()
        );
    }

    /**
     * 현재 판매가 추출
     *
     * CouponApplicationPrice가 아니라
     * 화면의 일반 판매 가격을 기준으로 한다.
     */
    private Long extractSalePrice(
            final WebElement courseElement
    ) {

        final List<WebElement> elements =
                courseElement.findElements(
                        By.cssSelector(
                                ".lecture-price > strong"
                        )
                );

        if (elements.isEmpty()) {
            return null;
        }

        return this.parsePrice(
                elements
                        .get(0)
                        .getText()
        );
    }

    /**
     * 상세페이지 URL 추출
     */
    private String extractDetailUrl(
            final WebElement courseElement
    ) {

        final List<WebElement> nameElements =
                courseElement.findElements(
                        By.cssSelector(
                                ".lecture-name"
                        )
                );

        if (nameElements.isEmpty()) {
            return null;
        }

        final WebElement nameElement =
                nameElements.get(0);

        final String href =
                nameElement.getAttribute(
                        "href"
                );

        if (href != null
                && !href.isBlank()
                && !"#".equals(href)) {

            if (href.startsWith("http")) {
                return href;
            }

            return BASE_URL
                    + href;
        }

        final String onclick =
                nameElement.getAttribute(
                        "onclick"
                );

        if (onclick == null
                || onclick.isBlank()) {
            return null;
        }

        return this.extractUrlFromOnclick(
                onclick
        );
    }

    /**
     * onclick 속성에서 location.href URL 추출
     */
    private String extractUrlFromOnclick(
            final String onclick
    ) {

        final String marker =
                "location.href='";

        final int markerIndex =
                onclick.indexOf(
                        marker
                );

        if (markerIndex < 0) {
            return null;
        }

        final int startIndex =
                markerIndex
                        + marker.length();

        final int endIndex =
                onclick.indexOf(
                        "'",
                        startIndex
                );

        if (endIndex < 0) {
            return null;
        }

        final String url =
                onclick.substring(
                        startIndex,
                        endIndex
                );

        if (url.startsWith("http")) {
            return url;
        }

        return BASE_URL
                + url;
    }

    /**
     * 자격증명 비교
     */
    private boolean isRelatedCourse(
            final String courseName,
            final String qualificationName
    ) {

        final String normalizedCourseName =
                this.normalizeName(
                        courseName
                );

        final String normalizedQualificationName =
                this.normalizeName(
                        qualificationName
                );

        return normalizedCourseName.contains(
                normalizedQualificationName
        );
    }

    /**
     * 이름 비교용 정규화
     */
    private String normalizeName(
            final String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "[\\s()·ㆍ\\-]",
                        ""
                )
                .trim();
    }

    /**
     * 정가와 판매가가 같으면 할인가 없음
     */
    private Long normalizeDiscountPrice(
            final Long originalPrice,
            final Long salePrice
    ) {

        if (salePrice == null) {
            return null;
        }

        if (originalPrice == null) {
            return null;
        }

        if (salePrice >= originalPrice) {
            return null;
        }

        return salePrice;
    }

    /**
     * "379,000원" → 379000
     */
    private Long parsePrice(
            final String priceText
    ) {

        if (priceText == null
                || priceText.isBlank()) {
            return null;
        }

        final String numberText =
                priceText.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (numberText.isBlank()) {
            return null;
        }

        return Long.parseLong(
                numberText
        );
    }

    /**
     * XPath 문자열 안전 처리
     */
    private String toXPathLiteral(
            final String value
    ) {

        if (!value.contains("'")) {
            return "'"
                    + value
                    + "'";
        }

        if (!value.contains("\"")) {
            return "\""
                    + value
                    + "\"";
        }

        final String[] parts =
                value.split("'");

        final StringBuilder builder =
                new StringBuilder(
                        "concat("
                );

        for (int index = 0;
             index < parts.length;
             index++) {

            if (index > 0) {
                builder.append(
                        ", \"'\", "
                );
            }

            builder.append("'")
                    .append(parts[index])
                    .append("'");
        }

        builder.append(")");

        return builder.toString();
    }
}

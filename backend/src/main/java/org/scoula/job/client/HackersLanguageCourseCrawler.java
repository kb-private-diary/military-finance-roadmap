package org.scoula.job.client;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
public class HackersLanguageCourseCrawler {

    private static final String CHAMP_BASE_URL =
            "https://champ.hackers.com";

    private static final String TOEIC_URL =
            CHAMP_BASE_URL
                    + "/?r=champstudy&c=lecture/lec_toeic";

    private static final String SPEAKING_URL =
            CHAMP_BASE_URL
                    + "/?r=champstudy"
                    + "&c=lecture%2Flec_speak"
                    + "&cate1_nm=%ED%86%A0%EC%8A%A4%2F%EC%98%A4%ED%94%BD"
                    + "&cate2_cd=sf011018%2Csf011017%2Csf011008"
                    + "&cate3_cd="
                    + "&tc_id="
                    + "&tc_chk_id="
                    + "&book_id="
                    + "&level=sf011018"
                    + "&level=sf011017"
                    + "&level=sf011008";

    private static final String CHINA_BASE_URL =
            "https://china.hackers.com";

    private static final String CHINA_URL =
            CHINA_BASE_URL
                    + "/?r=china&c=leclist";

    private static final String JAPAN_BASE_URL =
            "https://japan.hackers.com";

    private static final String JAPAN_URL =
            JAPAN_BASE_URL
                    + "/?r=japan&c=lecture";

    private static final String COURSE_TYPE = "C01";
    private static final String PROVIDER_NAME = "해커스";

    private static final int TIMEOUT_MILLIS = 10_000;
    private static final int WAIT_SECONDS = 10;
    private static final int MAX_COURSE_COUNT = 3;

    // =========================================================
    // TOEIC
    // =========================================================

    public List<JobCourseVO> crawlToeicCourses() {

        return this.crawlChampCourses(
                TOEIC_URL,
                "TOEIC"
        );
    }

    // =========================================================
    // TOEIC Speaking
    // =========================================================

    public List<JobCourseVO> crawlToeicSpeakingCourses() {

        return this.crawlChampCourses(
                SPEAKING_URL,
                "TOEIC_SPEAKING"
        );
    }

    // =========================================================
    // OPIc
    // =========================================================

    public List<JobCourseVO> crawlOpicCourses() {

        return this.crawlChampCourses(
                SPEAKING_URL,
                "OPIC"
        );
    }

    // =========================================================
// HSK 5급 / 6급
// =========================================================

    public List<JobCourseVO> crawlHskCourses(
            final String qualificationName
    ) {

        final WebDriver driver =
                new ChromeDriver(
                        this.createChromeOptions()
                );

        try {

            final WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    WAIT_SECONDS
                            )
                    );

            driver.get(
                    CHINA_URL
            );

            /*
             * 실제 DOM
             *
             * HSK 5급
             * <input
             *     type="checkbox"
             *     name="level"
             *     value="sf012"
             * >
             *
             * HSK 6급
             * <input
             *     type="checkbox"
             *     name="level"
             *     value="sf013"
             * >
             */
            final String levelValue;

            if ("HSK 5급".equals(
                    qualificationName
            )) {

                levelValue = "sf012";

            } else if ("HSK 6급".equals(
                    qualificationName
            )) {

                levelValue = "sf013";

            } else {

                log.info(
                        "지원하지 않는 HSK 급수 - qualificationName: {}",
                        qualificationName
                );

                return List.of();
            }

            final WebElement hskLevelCheckbox =
                    wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.cssSelector(
                                            "input[name='level'][value='"
                                                    + levelValue
                                                    + "']"
                                    )
                            )
                    );

            if (!hskLevelCheckbox.isSelected()) {

                this.clickElement(
                        driver,
                        hskLevelCheckbox
                );
            }

            log.info(
                    "해커스 HSK 급수 선택 완료 - qualificationName: {}, levelValue: {}, selected: {}",
                    qualificationName,
                    levelValue,
                    hskLevelCheckbox.isSelected()
            );

            /*
             * 조회 버튼 대신 사이트 함수 직접 실행
             */
            ((JavascriptExecutor) driver)
                    .executeScript(
                            "goLectureList();"
                    );

            /*
             * 조회 결과에 해당 급수 강의가 나타날 때까지 기다림
             */
            final String expectedText =
                    qualificationName.replaceAll(
                            "\\s+",
                            ""
                    );

            wait.until(
                    webDriver -> {

                        final List<WebElement> nameElements =
                                webDriver.findElements(
                                        By.cssSelector(
                                                ".row .txt h5 a"
                                        )
                                );

                        return nameElements.stream()
                                .map(WebElement::getText)
                                .map(
                                        courseName ->
                                                courseName.replaceAll(
                                                        "\\s+",
                                                        ""
                                                )
                                )
                                .anyMatch(
                                        courseName ->
                                                courseName.contains(
                                                        expectedText
                                                )
                                );
                    }
            );

            final Document document =
                    Jsoup.parse(
                            driver.getPageSource(),
                            driver.getCurrentUrl()
                    );

            final Elements courseElements =
                    document.select(
                            ".row"
                    );

            log.info(
                    "해커스 HSK 조회 결과 DOM - qualificationName: {}, elementCount: {}",
                    qualificationName,
                    courseElements.size()
            );

            final List<JobCourseVO> courses =
                    this.parseHskCourses(
                            courseElements,
                            qualificationName
                    );

            log.info(
                    "해커스 HSK 강의 조회 완료 - qualificationName: {}, count: {}",
                    qualificationName,
                    courses.size()
            );

            return courses;

        } catch (TimeoutException e) {

            log.info(
                    "해커스 HSK 강의 조회 시간 초과 - qualificationName: {}",
                    qualificationName
            );

            return List.of();

        } catch (Exception e) {

            log.warn(
                    "해커스 HSK 강의 크롤링 실패 - qualificationName: {}",
                    qualificationName,
                    e
            );

            return List.of();

        } finally {

            driver.quit();
        }
    }

    private List<JobCourseVO> parseHskCourses(
            final Elements courseElements,
            final String qualificationName
    ) {

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element courseElement
                : courseElements) {

            if (courses.size()
                    >= MAX_COURSE_COUNT) {
                break;
            }

            final Element nameElement =
                    courseElement.selectFirst(
                            ".txt h5 a"
                    );

            if (nameElement == null) {
                continue;
            }

            final String courseName =
                    nameElement.text()
                            .trim();

            log.info(
                    "해커스 HSK 후보 - qualificationName: {}, courseName: {}",
                    qualificationName,
                    courseName
            );

            if (!this.matchesHskCourse(
                    courseName,
                    qualificationName
            )) {
                continue;
            }

            final Element originalPriceElement =
                    courseElement.selectFirst(
                            "ul.price li.buy del"
                    );

            Element salePriceElement =
                    courseElement.selectFirst(
                            "ul.price li.buy strong.price_sale"
                    );

            if (salePriceElement == null) {

                salePriceElement =
                        courseElement.selectFirst(
                                "ul.price li.buy strong.won"
                        );
            }

            final Long originalPrice =
                    this.parsePrice(
                            originalPriceElement
                    );

            final Long salePrice =
                    this.parseSalePrice(
                            salePriceElement
                    );

            if (originalPrice == null
                    && salePrice == null) {
                continue;
            }

            final String detailUrl =
                    this.resolveUrl(
                            CHINA_BASE_URL,
                            nameElement.attr(
                                    "href"
                            )
                    );

            courses.add(
                    this.createCourse(
                            courseName,
                            originalPrice,
                            salePrice,
                            detailUrl
                    )
            );
        }

        return courses;
    }

    private boolean matchesHskCourse(
            final String courseName,
            final String qualificationName
    ) {

        final String normalizedCourseName =
                this.normalizeName(
                        courseName
                );

        if ("HSK 5급".equals(
                qualificationName
        )) {

            return normalizedCourseName.contains(
                    "HSK5급"
            );
        }

        if ("HSK 6급".equals(
                qualificationName
        )) {

            return normalizedCourseName.contains(
                    "HSK6급"
            );
        }

        return false;
    }

    // =========================================================
    // JLPT N1 / N2
    // =========================================================

    public List<JobCourseVO> crawlJlptCourses(
            final String qualificationName
    ) {

        final WebDriver driver =
                new ChromeDriver(
                        this.createChromeOptions()
                );

        try {

            final WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    WAIT_SECONDS
                            )
                    );

            driver.get(
                    JAPAN_URL
            );

            /*
             * 1. 과목 JLPT 선택
             */
            final WebElement jlptCheckbox =
                    wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath(
                                            "//label"
                                                    + "[.//span[contains(@class,'smartfinder-name') "
                                                    + "and normalize-space()='JLPT']]"
                                                    + "//input"
                                    )
                            )
                    );

            if (!jlptCheckbox.isSelected()) {

                this.clickElement(
                        driver,
                        jlptCheckbox
                );
            }

            /*
             * 2. N1 또는 N2 선택
             */
            final String level =
                    this.resolveJlptLevel(
                            qualificationName
                    );

            final WebElement levelCheckbox =
                    wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.xpath(
                                            "//label"
                                                    + "[.//span[contains(@class,'smartfinder-name') "
                                                    + "and normalize-space()='"
                                                    + level
                                                    + "']]"
                                                    + "//input"
                                    )
                            )
                    );

            if (!levelCheckbox.isSelected()) {

                this.clickElement(
                        driver,
                        levelCheckbox
                );
            }

            log.info(
                    "해커스 JLPT 필터 선택 완료 - qualificationName: {}, level: {}",
                    qualificationName,
                    level
            );

            /*
             * 실제 조회 버튼:
             *
             * onclick="
             *   smartFinderControl.getLectureList();
             *   return false;
             * "
             */
            ((JavascriptExecutor) driver)
                    .executeScript(
                            "smartFinderControl.getLectureList();"
                    );

            final String expectedText =
                    "JLPT " + level;

            wait.until(
                    webDriver -> {

                        final List<WebElement> elements =
                                webDriver.findElements(
                                        By.cssSelector(
                                                ".lecture-list "
                                                        + ".lecture-item "
                                                        + ".lecture-name"
                                        )
                                );

                        return elements.stream()
                                .map(WebElement::getText)
                                .anyMatch(
                                        courseName ->
                                                courseName.contains(
                                                        expectedText
                                                )
                                );
                    }
            );

            final Document document =
                    Jsoup.parse(
                            driver.getPageSource(),
                            driver.getCurrentUrl()
                    );

            final Elements courseElements =
                    document.select(
                            ".lecture-list .lecture-item"
                    );

            log.info(
                    "해커스 JLPT 조회 결과 DOM - qualificationName: {}, elementCount: {}",
                    qualificationName,
                    courseElements.size()
            );

            final List<JobCourseVO> courses =
                    this.parseJapaneseCourses(
                            courseElements,
                            qualificationName
                    );

            log.info(
                    "해커스 JLPT 강의 조회 완료 - qualificationName: {}, count: {}",
                    qualificationName,
                    courses.size()
            );

            return courses;

        } catch (TimeoutException e) {

            log.info(
                    "해커스 JLPT 강의 조회 시간 초과 - qualificationName: {}",
                    qualificationName
            );

            return List.of();

        } catch (Exception e) {

            log.warn(
                    "해커스 JLPT 강의 크롤링 실패 - qualificationName: {}",
                    qualificationName,
                    e
            );

            return List.of();

        } finally {

            driver.quit();
        }
    }

    private String resolveJlptLevel(
            final String qualificationName
    ) {

        if (qualificationName != null
                && qualificationName.contains(
                "N2"
        )) {

            return "N2";
        }

        return "N1";
    }

    private List<JobCourseVO> parseJapaneseCourses(
            final Elements courseElements,
            final String qualificationName
    ) {

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element courseElement
                : courseElements) {

            if (courses.size()
                    >= MAX_COURSE_COUNT) {
                break;
            }

            final Element nameElement =
                    courseElement.selectFirst(
                            ".lecture-name"
                    );

            if (nameElement == null) {
                continue;
            }

            final String courseName =
                    nameElement.text()
                            .trim();

            if (!this.matchesJlptCourse(
                    courseName,
                    qualificationName
            )) {
                continue;
            }

            final Element originalPriceElement =
                    courseElement.selectFirst(
                            ".lecture-price del"
                    );

            final Element salePriceElement =
                    courseElement.selectFirst(
                            ".lecture-price strong"
                    );

            final Long originalPrice =
                    this.parsePrice(
                            originalPriceElement
                    );

            final Long salePrice =
                    this.parsePrice(
                            salePriceElement
                    );

            if (originalPrice == null
                    && salePrice == null) {
                continue;
            }

            final String detailUrl =
                    this.extractJapaneseDetailUrl(
                            nameElement
                    );

            courses.add(
                    this.createCourse(
                            courseName,
                            originalPrice,
                            salePrice,
                            detailUrl
                    )
            );
        }

        return courses;
    }

    private boolean matchesJlptCourse(
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

    // =========================================================
    // TOEIC / TOEIC Speaking / OPIc
    // =========================================================

    private List<JobCourseVO> crawlChampCourses(
            final String url,
            final String courseCategory
    ) {

        try {

            final Document document =
                    Jsoup.connect(
                                    url
                            )
                            .userAgent(
                                    "Mozilla/5.0"
                            )
                            .timeout(
                                    TIMEOUT_MILLIS
                            )
                            .get();

            final List<JobCourseVO> courses =
                    this.parseChampCourses(
                            document,
                            courseCategory
                    );

            log.info(
                    "해커스 어학 강의 조회 완료 - courseCategory: {}, count: {}",
                    courseCategory,
                    courses.size()
            );

            return courses;

        } catch (Exception e) {

            log.warn(
                    "해커스 어학 강의 크롤링 실패 - courseCategory: {}",
                    courseCategory,
                    e
            );

            return List.of();
        }
    }

    private List<JobCourseVO> parseChampCourses(
            final Document document,
            final String courseCategory
    ) {

        final List<JobCourseVO> courses =
                new ArrayList<>();

        final Elements courseElements =
                document.select(
                        ".article .cnt .row"
                );

        for (final Element courseElement
                : courseElements) {

            if (courses.size()
                    >= MAX_COURSE_COUNT) {
                break;
            }

            final Element nameElement =
                    courseElement.selectFirst(
                            ".txt h5 a"
                    );

            if (nameElement == null) {
                continue;
            }

            final String courseName =
                    nameElement.text()
                            .trim();

            if (!this.matchesChampCourse(
                    courseName,
                    courseCategory
            )) {
                continue;
            }

            final Element originalPriceElement =
                    courseElement.selectFirst(
                            "ul.price li.buy del"
                    );

            Element salePriceElement =
                    courseElement.selectFirst(
                            "ul.price li.buy strong.price_sale"
                    );

            if (salePriceElement == null) {

                salePriceElement =
                        courseElement.selectFirst(
                                "ul.price li.buy strong.won"
                        );
            }

            final Long originalPrice =
                    this.parsePrice(
                            originalPriceElement
                    );

            final Long salePrice =
                    this.parseSalePrice(
                            salePriceElement
                    );

            if (originalPrice == null
                    && salePrice == null) {
                continue;
            }

            final String detailUrl =
                    this.resolveUrl(
                            CHAMP_BASE_URL,
                            nameElement.attr(
                                    "href"
                            )
                    );

            courses.add(
                    this.createCourse(
                            courseName,
                            originalPrice,
                            salePrice,
                            detailUrl
                    )
            );
        }

        return courses;
    }

    private boolean matchesChampCourse(
            final String courseName,
            final String courseCategory
    ) {

        final String normalizedName =
                this.normalizeName(
                        courseName
                );

        if ("TOEIC".equals(
                courseCategory
        )) {

            return normalizedName.contains(
                    "토익"
            )
                    && !normalizedName.contains(
                    "토익스피킹"
            )
                    && !normalizedName.contains(
                    "토스"
            );
        }

        if ("TOEIC_SPEAKING".equals(
                courseCategory
        )) {

            return normalizedName.contains(
                    "토스"
            )
                    || normalizedName.contains(
                    "토익스피킹"
            )
                    || normalizedName.contains(
                    "TOEICSPEAKING"
            );
        }

        if ("OPIC".equals(
                courseCategory
        )) {

            return normalizedName.contains(
                    "오픽"
            )
                    || normalizedName.contains(
                    "OPIC"
            );
        }

        return false;
    }

    // =========================================================
    // Selenium
    // =========================================================

    private ChromeOptions createChromeOptions() {

        final ChromeOptions options =
                new ChromeOptions();

        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080",
                "--lang=ko-KR"
        );

        return options;
    }

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

    // =========================================================
    // JobCourseVO
    // =========================================================

    private JobCourseVO createCourse(
            final String courseName,
            final Long originalPrice,
            final Long salePrice,
            final String detailUrl
    ) {

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

        return course;
    }

    // =========================================================
    // 가격
    // =========================================================

    private Long parsePrice(
            final Element element
    ) {

        if (element == null) {
            return null;
        }

        return this.parsePrice(
                element.text()
        );
    }

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
     * strong 안의 할인율 span 제거 후 가격만 파싱
     */
    private Long parseSalePrice(
            final Element salePriceElement
    ) {

        if (salePriceElement == null) {
            return null;
        }

        final Element copiedElement =
                salePriceElement.clone();

        copiedElement.select(
                ".discount_rate"
        ).remove();

        return this.parsePrice(
                copiedElement.text()
        );
    }

    private Long normalizeDiscountPrice(
            final Long originalPrice,
            final Long salePrice
    ) {

        if (originalPrice == null
                || salePrice == null) {
            return null;
        }

        if (salePrice >= originalPrice) {
            return null;
        }

        return salePrice;
    }

    // =========================================================
    // URL
    // =========================================================

    private String extractJapaneseDetailUrl(
            final Element nameElement
    ) {

        final String href =
                nameElement.attr(
                                "href"
                        )
                        .trim();

        if (!href.isBlank()
                && !"#".equals(href)
                && !"#;".equals(href)) {

            return this.resolveUrl(
                    JAPAN_BASE_URL,
                    href
            );
        }

        final String onclick =
                nameElement.attr(
                        "onclick"
                );

        if (onclick.isBlank()) {
            return null;
        }

        final String singleQuoteMarker =
                "location.href='";

        final String doubleQuoteMarker =
                "location.href=\"";

        int markerIndex =
                onclick.indexOf(
                        singleQuoteMarker
                );

        String marker =
                singleQuoteMarker;

        char endQuote =
                '\'';

        if (markerIndex < 0) {

            markerIndex =
                    onclick.indexOf(
                            doubleQuoteMarker
                    );

            marker =
                    doubleQuoteMarker;

            endQuote =
                    '"';
        }

        if (markerIndex < 0) {
            return null;
        }

        final int startIndex =
                markerIndex
                        + marker.length();

        final int endIndex =
                onclick.indexOf(
                        endQuote,
                        startIndex
                );

        if (endIndex < 0) {
            return null;
        }

        final String detailPath =
                onclick.substring(
                        startIndex,
                        endIndex
                );

        return this.resolveUrl(
                JAPAN_BASE_URL,
                detailPath
        );
    }

    private String resolveUrl(
            final String baseUrl,
            final String href
    ) {

        if (href == null
                || href.isBlank()) {
            return null;
        }

        if (href.startsWith(
                "http"
        )) {
            return href;
        }

        if (href.startsWith(
                "/"
        )) {

            return baseUrl
                    + href;
        }

        return baseUrl
                + "/"
                + href;
    }

    // =========================================================
    // 문자열
    // =========================================================

    private String normalizeName(
            final String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "\\s+",
                        ""
                )
                .toUpperCase()
                .trim();
    }
}

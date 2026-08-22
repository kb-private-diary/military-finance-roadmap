package org.scoula.job.client;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.scoula.job.dto.JobCourseCrawlDTO;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Log4j2
public class InflearnCrawlerClient {

    private static final String BASE_URL = "https://www.inflearn.com";
    private static final String SEARCH_URL = BASE_URL + "/search?s=";

    private static final int MAX_COURSE_COUNT = 3;
    private static final int WAIT_SECONDS = 15;

    /**
     * 여러 검색어를 하나의 ChromeDriver로 연속 크롤링한다.
     */
    public Map<String, List<JobCourseCrawlDTO>> findCoursesBatch(
            final List<String> keywords
    ) {

        final Map<String, List<JobCourseCrawlDTO>> result =
                new LinkedHashMap<>();

        final ChromeOptions options =
                this.createChromeOptions();

        final WebDriver driver =
                new ChromeDriver(options);

        try {

            for (final String keyword : keywords) {

                final List<JobCourseCrawlDTO> courses =
                        this.findCourses(
                                driver,
                                keyword
                        );

                result.put(
                        keyword,
                        courses
                );
            }

        } finally {

            driver.quit();
        }

        return result;
    }
    /**
     * 단일 검색어를 크롤링한다.
     */
    public List<JobCourseCrawlDTO> findCourses(
            final String keyword
    ) {

        final ChromeOptions options =
                this.createChromeOptions();

        final WebDriver driver =
                new ChromeDriver(options);

        try {

            return this.findCourses(
                    driver,
                    keyword
            );

        } finally {

            driver.quit();
        }
    }

    /**
     * 하나의 ChromeDriver를 사용해
     * 검색어 하나를 크롤링한다.
     */
    private List<JobCourseCrawlDTO> findCourses(
            final WebDriver driver,
            final String keyword
    ) {

        final List<JobCourseCrawlDTO> courses =
                new ArrayList<>();

        try {

            final String encodedKeyword =
                    URLEncoder.encode(
                            keyword,
                            StandardCharsets.UTF_8
                    );

            final String searchUrl =
                    SEARCH_URL + encodedKeyword;

            log.info(
                    "인프런 검색 시작 - keyword: {}, url: {}",
                    keyword,
                    searchUrl
            );

            driver.get(searchUrl);

            final WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(WAIT_SECONDS)
                    );

            wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector(
                                    "[data-course-item='true']"
                            )
                    )
            );

            final List<WebElement> courseItems =
                    driver.findElements(
                            By.cssSelector(
                                    "[data-course-item='true']"
                            )
                    );

            log.info(
                    "인프런 검색 결과 카드 수 - keyword: {}, count: {}",
                    keyword,
                    courseItems.size()
            );

            final List<CourseCandidate> candidates =
                    new ArrayList<>();

            final Set<String> visitedUrls =
                    new HashSet<>();

            for (final WebElement courseItem
                    : courseItems) {

                if (candidates.size()
                        >= MAX_COURSE_COUNT) {

                    break;
                }

                final WebElement link;

                try {

                    link =
                            courseItem.findElement(
                                    By.cssSelector(
                                            "a[href*='/course/']"
                                    )
                            );

                } catch (NoSuchElementException e) {

                    continue;
                }

                String detailUrl =
                        link.getAttribute(
                                "href"
                        );

                if (detailUrl == null
                        || detailUrl.isBlank()) {

                    continue;
                }

                detailUrl =
                        this.removeQueryString(
                                detailUrl
                        );

                if (!visitedUrls.add(
                        detailUrl
                )) {

                    continue;
                }

                final String courseName =
                        courseItem
                                .findElements(
                                        By.tagName("p")
                                )
                                .stream()
                                .map(
                                        WebElement::getText
                                )
                                .map(
                                        String::trim
                                )
                                .filter(
                                        text ->
                                                text.contains(
                                                        keyword
                                                )
                                )
                                .findFirst()
                                .orElse(null);

                if (courseName == null
                        || courseName.isBlank()) {

                    continue;
                }

                candidates.add(
                        new CourseCandidate(
                                courseName,
                                detailUrl
                        )
                );

                log.info(
                        "인프런 강의 후보 수집 - courseName: {}, url: {}",
                        courseName,
                        detailUrl
                );
            }

            for (final CourseCandidate candidate
                    : candidates) {

                final JobCourseCrawlDTO course =
                        this.findCourseDetail(
                                driver,
                                candidate.courseName(),
                                candidate.detailUrl()
                        );

                if (course != null) {

                    courses.add(
                            course
                    );
                }
            }

            log.info(
                    "인프런 크롤링 완료 - keyword: {}, count: {}",
                    keyword,
                    courses.size()
            );

        } catch (Exception e) {

            log.warn(
                    "인프런 크롤링 실패 - keyword: {}",
                    keyword,
                    e
            );
        }

        return courses;
    }
    /**
     * ChromeDriver 실행 옵션을 생성한다.
     */
    private ChromeOptions createChromeOptions() {

        final ChromeOptions options =
                new ChromeOptions();

        // headless 사용 시 인프런에서 403이 발생하므로 사용하지 않는다.
        options.addArguments(
                "--window-size=1920,1080"
        );

        options.addArguments(
                "--no-sandbox"
        );

        options.addArguments(
                "--disable-dev-shm-usage"
        );

        return options;
    }

    /**
     * 강의 상세페이지에서 정가와 판매가를 조회한다.
     */
    private JobCourseCrawlDTO findCourseDetail(
            WebDriver driver,
            String courseName,
            String detailUrl
    ) {
        try {
            driver.get(detailUrl);

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(WAIT_SECONDS)
                    );

            // 상세페이지 기본 콘텐츠가 로드될 때까지 대기
            wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.tagName("body")
                    )
            );

            /*
             * 가격은 JavaScript 렌더링 후 나타날 수 있으므로
             * 정가 또는 판매가 영역이 나타날 때까지 잠시 기다린다.
             */
            try {
                wait.until(
                        driver1 ->
                                !driver1.findElements(
                                        By.cssSelector(
                                                "p[aria-label='정가'] span"
                                        )
                                ).isEmpty()
                                        ||
                                        !driver1.findElements(
                                                By.cssSelector(
                                                        "p[aria-label='판매가'] span"
                                                )
                                        ).isEmpty()
                );
            } catch (TimeoutException e) {
                log.warn(
                        "인프런 가격 영역 대기 시간 초과 - courseName: {}",
                        courseName
                );
            }

            Long originalPrice =
                    extractPrice(driver, "정가");

            Long discountPrice =
                    extractPrice(driver, "판매가");

            /*
             * 정가가 없고 판매가만 있는 경우
             * 해당 판매가를 기본 가격으로 처리한다.
             */
            if (originalPrice == null && discountPrice != null) {
                originalPrice = discountPrice;
                discountPrice = null;
            }

            /*
             * 정가와 판매가가 동일하면 실제 할인 가격이 아니므로
             * discountPrice는 저장하지 않는다.
             */
            if (originalPrice != null
                    && originalPrice.equals(discountPrice)) {
                discountPrice = null;
            }

            log.info(
                    "인프런 상세 조회 완료 - courseName: {}, originalPrice: {}, discountPrice: {}",
                    courseName,
                    originalPrice,
                    discountPrice
            );

            return JobCourseCrawlDTO.builder()
                    .courseName(courseName)
                    .providerName("인프런")
                    .originalPrice(originalPrice)
                    .discountPrice(discountPrice)
                    .detailUrl(detailUrl)
                    .build();

        } catch (Exception e) {
            log.warn(
                    "인프런 상세 페이지 크롤링 실패 - courseName: {}, url: {}",
                    courseName,
                    detailUrl,
                    e
            );

            return null;
        }
    }

    /**
     * aria-label을 기준으로 가격을 조회한다.
     *
     * 예)
     * 정가   -> ₩44,000
     * 판매가 -> ₩33,000
     */
    private Long extractPrice(
            WebDriver driver,
            String ariaLabel
    ) {
        List<WebElement> priceElements =
                driver.findElements(
                        By.cssSelector(
                                "p[aria-label='" + ariaLabel + "'] span"
                        )
                );

        if (priceElements.isEmpty()) {
            return null;
        }

        String priceText =
                priceElements.get(0)
                        .getText()
                        .replaceAll("[^0-9]", "");

        if (priceText.isBlank()) {
            return null;
        }

        return Long.parseLong(priceText);
    }

    /**
     * 검색 결과에 붙는 attributionToken 등 쿼리스트링을 제거한다.
     */
    private String removeQueryString(String url) {
        int queryIndex = url.indexOf("?");

        if (queryIndex < 0) {
            return url;
        }

        return url.substring(0, queryIndex);
    }

    /**
     * 검색 결과에서 상세 조회 전에 임시로 사용할 강의 정보.
     */
    private record CourseCandidate(
            String courseName,
            String detailUrl
    ) {
    }
}

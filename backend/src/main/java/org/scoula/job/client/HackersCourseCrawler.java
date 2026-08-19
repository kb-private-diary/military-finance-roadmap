package org.scoula.job.client;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scoula.job.domain.JobCourseVO;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Log4j2
public class HackersCourseCrawler {

    // 일반직 9급 / 전산직
    private static final String PUBLIC_COURSE_URL =
            "https://egosi.hackers.com/site/?c=event&evt_cd=EG4244284222";

    // 군무원
    private static final String MILITARY_COURSE_URL =
            "https://earmy.hackers.com/event/4245232122";

    // 경찰
    private static final String POLICE_COURSE_URL =
            "https://epolice.hackers.com/site/?c=event&evt_cd=EP4247263722";

    private static final String COURSE_TYPE = "C02";
    private static final String PROVIDER_NAME = "해커스";

    private static final int TIMEOUT_MILLIS = 10_000;
    private static final int TARGET_YEAR_COUNT = 2;

    private static final Pattern YEAR_PATTERN =
            Pattern.compile("(\\d{2})년|(?<!\\d)(\\d{2})대비");

    private static final Pattern PRICE_PATTERN =
            Pattern.compile("(\\d{1,3}(?:,\\d{3})+)\\s*원");

    /**
     * 해커스 공무원 인강 전체 수집
     *
     * - 일반직 9급
     * - 전산직
     * - 군무원 9급
     * - 경찰
     */
    public List<JobCourseVO> crawlCourses()
            throws IOException {

        final List<JobCourseVO> courses =
                new ArrayList<>();

        courses.addAll(
                this.crawlPublicCourses()
        );

        courses.addAll(
                this.crawlTechnicalCourses()
        );

        courses.addAll(
                this.crawlMilitaryCourses()
        );

        courses.addAll(
                this.crawlPoliceCourses()
        );

        return courses;
    }

    /**
     * 일반직 9급
     *
     * 27년 대비 9급 0원 패스
     * 28년 대비 9급 0원 패스
     *
     * 7·9급 상품은 제외한다.
     */
    private List<JobCourseVO> crawlPublicCourses()
            throws IOException {

        final Document document =
                this.connect(PUBLIC_COURSE_URL);

        final Elements productElements =
                document.select(".pass_tab_area li");

        final List<JobCourseVO> candidates =
                new ArrayList<>();

        for (final Element productElement : productElements) {

            final Element titleElement =
                    productElement.selectFirst(".title_box");

            if (titleElement == null) {
                continue;
            }

            final String courseName =
                    titleElement.text().trim();

            if (!this.isPublicCourse(courseName)) {
                continue;
            }

            final Element originalPriceElement =
                    productElement.selectFirst(".before_price");

            final Element discountPriceElement =
                    productElement.selectFirst(".after_price");

            if (originalPriceElement == null
                    || discountPriceElement == null) {

                log.warn(
                        "해커스 일반직 가격 영역을 찾을 수 없습니다: {}",
                        courseName
                );

                continue;
            }

            final JobCourseVO course =
                    this.createCourse(
                            courseName,
                            originalPriceElement.text(),
                            discountPriceElement.text(),
                            PUBLIC_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                continue;
            }

            if (!this.containsSameCourse(
                    candidates,
                    course
            )) {
                candidates.add(course);
            }
        }

        return this.filterLatestYears(
                candidates
        );
    }

    /**
     * 전산직
     */
    private List<JobCourseVO> crawlTechnicalCourses()
            throws IOException {

        final Document document =
                this.connect(PUBLIC_COURSE_URL);

        final Elements productElements =
                document.select(".pass_tab_area li");

        final List<JobCourseVO> candidates =
                new ArrayList<>();

        for (final Element productElement : productElements) {

            final Element titleElement =
                    productElement.selectFirst(".title_box");

            if (titleElement == null) {
                continue;
            }

            final String courseName =
                    titleElement.text().trim();

            if (!this.isTechnicalCourse(courseName)) {
                continue;
            }

            final Element originalPriceElement =
                    productElement.selectFirst(".before_price");

            final Element discountPriceElement =
                    productElement.selectFirst(".after_price");

            if (originalPriceElement == null
                    || discountPriceElement == null) {

                log.warn(
                        "해커스 전산직 가격 영역을 찾을 수 없습니다: {}",
                        courseName
                );

                continue;
            }

            final JobCourseVO course =
                    this.createCourse(
                            courseName,
                            originalPriceElement.text(),
                            discountPriceElement.text(),
                            PUBLIC_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                continue;
            }

            if (!this.containsSameCourse(
                    candidates,
                    course
            )) {
                candidates.add(course);
            }
        }

        return this.filterLatestYears(
                candidates
        );
    }

    /**
     * 군무원
     *
     * 9급 + 직렬 선택형 상품만 수집한다.
     * 9·7급 / 9•7급 상품은 제외한다.
     */
    private List<JobCourseVO> crawlMilitaryCourses()
            throws IOException {

        final Document document =
                this.connect(MILITARY_COURSE_URL);

        final Elements productElements =
                document.select(
                        "div[class*=\"lectItem\"]"
                );

        final List<JobCourseVO> candidates =
                new ArrayList<>();

        for (final Element productElement : productElements) {

            final Element titleElement =
                    productElement.selectFirst(
                            "p[class*=\"lectItemTitle\"]"
                    );

            if (titleElement == null) {
                continue;
            }

            final String courseName =
                    titleElement.text().trim();

            if (!this.isMilitaryCourse(courseName)) {
                continue;
            }

            final Element originalPriceElement =
                    productElement.selectFirst(
                            "[class*=\"originPrice\"]"
                    );

            final Element discountPriceElement =
                    productElement.selectFirst(
                            "[class*=\"discountPrice\"]"
                    );

            if (originalPriceElement == null
                    || discountPriceElement == null) {

                log.warn(
                        "해커스 군무원 가격 영역을 찾을 수 없습니다: {}",
                        courseName
                );

                continue;
            }

            final JobCourseVO course =
                    this.createCourse(
                            courseName,
                            originalPriceElement.text(),
                            discountPriceElement.text(),
                            MILITARY_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                continue;
            }

            if (!this.containsSameCourse(
                    candidates,
                    course
            )) {
                candidates.add(course);
            }
        }

        return this.filterLatestYears(
                candidates
        );
    }

    /**
     * 경찰
     *
     * 최신 연도의 1차 / 2차 상품만 유지한다.
     */
    private List<JobCourseVO> crawlPoliceCourses()
            throws IOException {

        final Document document =
                this.connect(POLICE_COURSE_URL);

        final Elements productElements =
                document.select(".pass_area li");

        final List<JobCourseVO> candidates =
                new ArrayList<>();

        for (final Element productElement : productElements) {

            if (productElement
                    .text()
                    .contains("COMING SOON")) {
                continue;
            }

            final Element titleImage =
                    productElement.selectFirst(
                            "img[alt*=\"기적의 합격패스\"]"
                    );

            if (titleImage == null) {
                continue;
            }

            final String courseName =
                    titleImage
                            .attr("alt")
                            .trim();

            if (!this.isPoliceCourse(courseName)) {
                continue;
            }

            final Element originalPriceElement =
                    productElement.selectFirst(
                            ".before_price"
                    );

            final Element discountPriceElement =
                    productElement.selectFirst(
                            ".after_price"
                    );

            if (originalPriceElement == null
                    || discountPriceElement == null) {

                log.warn(
                        "해커스 경찰 가격 영역을 찾을 수 없습니다: {}",
                        courseName
                );

                continue;
            }

            final JobCourseVO course =
                    this.createCourse(
                            courseName,
                            originalPriceElement.text(),
                            discountPriceElement.text(),
                            POLICE_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                continue;
            }

            if (!this.containsSameCourse(
                    candidates,
                    course
            )) {
                candidates.add(course);
            }
        }

        return this.filterLatestPoliceYear(
                candidates
        );
    }

    /**
     * 일반직 9급 상품 여부
     */
    private boolean isPublicCourse(
            final String courseName
    ) {

        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains(
                "9급 0원 패스"
        )
                && !courseName.contains(
                "7·9급"
        )
                && !courseName.contains(
                "7•9급"
        )
                && !courseName.contains(
                "전산직"
        )
                && !courseName.contains(
                "군무원"
        );
    }

    /**
     * 전산직 상품 여부
     */
    private boolean isTechnicalCourse(
            final String courseName
    ) {

        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains(
                "전산직 패스"
        );
    }

    /**
     * 군무원 9급 직렬 선택형 상품 여부
     */
    private boolean isMilitaryCourse(
            final String courseName
    ) {

        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains(
                "9급 군무원"
        )
                && courseName.contains(
                "기적의 패스"
        )
                && courseName.contains(
                "[직렬 선택형]"
        )
                && !courseName.contains(
                "9•7급"
        )
                && !courseName.contains(
                "9·7급"
        );
    }

    /**
     * 경찰 상품 여부
     */
    private boolean isPoliceCourse(
            final String courseName
    ) {

        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains(
                "기적의 합격패스"
        )
                && !courseName.contains(
                "COMING SOON"
        );
    }

    /**
     * 경찰은 최신 연도 상품만 유지한다.
     *
     * 예:
     * 27년 1차
     * 27년 2차
     *
     * → 둘 다 유지
     * → 26년 상품은 제외
     */
    private List<JobCourseVO> filterLatestPoliceYear(
            final List<JobCourseVO> courses
    ) {

        final Integer latestYear =
                courses.stream()
                        .map(
                                course ->
                                        this.parseYear(
                                                course.getCourseName()
                                        )
                        )
                        .filter(
                                year -> year != null
                        )
                        .max(
                                Integer::compareTo
                        )
                        .orElse(null);

        if (latestYear == null) {
            return courses;
        }

        return courses.stream()
                .filter(
                        course ->
                                latestYear.equals(
                                        this.parseYear(
                                                course.getCourseName()
                                        )
                                )
                )
                .collect(
                        Collectors.toList()
                );
    }

    /**
     * 일반직 / 전산직 / 군무원은
     * 최신 2개 연도 상품을 유지한다.
     */
    private List<JobCourseVO> filterLatestYears(
            final List<JobCourseVO> courses
    ) {

        final List<Integer> targetYears =
                courses.stream()
                        .map(
                                course ->
                                        this.parseYear(
                                                course.getCourseName()
                                        )
                        )
                        .filter(
                                year -> year != null
                        )
                        .distinct()
                        .sorted(
                                Comparator.reverseOrder()
                        )
                        .limit(
                                TARGET_YEAR_COUNT
                        )
                        .collect(
                                Collectors.toList()
                        );

        if (targetYears.isEmpty()) {
            return courses;
        }

        return courses.stream()
                .filter(
                        course -> {

                            final Integer year =
                                    this.parseYear(
                                            course.getCourseName()
                                    );

                            return year != null
                                    && targetYears.contains(
                                    year
                            );
                        }
                )
                .collect(
                        Collectors.toList()
                );
    }

    /**
     * 상품명에서 연도 추출
     *
     * 예:
     * 27년 대비 ...
     * 28년 대비 ...
     */
    private Integer parseYear(
            final String text
    ) {

        if (text == null
                || text.isBlank()) {
            return null;
        }

        final Matcher matcher =
                YEAR_PATTERN.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        final String yearText =
                matcher.group(1) != null
                        ? matcher.group(1)
                        : matcher.group(2);

        return Integer.parseInt(
                yearText
        );
    }

    /**
     * 크롤링 결과 → JobCourseVO 변환
     */
    private JobCourseVO createCourse(
            final String courseName,
            final String originalPriceText,
            final String discountPriceText,
            final String detailUrl
    ) {

        final JobCourseVO courseVO =
                new JobCourseVO();

        courseVO.setCourseType(
                COURSE_TYPE
        );

        courseVO.setProviderName(
                PROVIDER_NAME
        );

        courseVO.setCourseName(
                courseName
        );

        courseVO.setOriginalPrice(
                this.parsePrice(
                        originalPriceText
                )
        );

        courseVO.setDiscountPrice(
                this.parsePrice(
                        discountPriceText
                )
        );

        courseVO.setMilitaryPrice(null);
        courseVO.setSelectedCost(null);

        courseVO.setDetailUrl(
                detailUrl
        );

        return courseVO;
    }

    /**
     * 가격 문자열 → Long
     *
     * 예:
     * "1,049,000원" → 1049000
     */
    private Long parsePrice(
            final String price
    ) {

        if (price == null
                || price.isBlank()) {
            return 0L;
        }

        final Matcher matcher =
                PRICE_PATTERN.matcher(price);

        Long result = null;

        while (matcher.find()) {

            result =
                    Long.parseLong(
                            matcher
                                    .group(1)
                                    .replace(
                                            ",",
                                            ""
                                    )
                    );
        }

        if (result != null) {
            return result;
        }

        final String number =
                price.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (number.isEmpty()) {
            return 0L;
        }

        return Long.parseLong(
                number
        );
    }

    /**
     * 정상적인 상품 데이터인지 확인
     */
    private boolean isValidCourse(
            final JobCourseVO course
    ) {

        return course.getCourseName() != null
                && !course
                .getCourseName()
                .isBlank()
                && course.getOriginalPrice() != null
                && course.getOriginalPrice() > 0
                && course.getDiscountPrice() != null
                && course.getDiscountPrice() > 0;
    }

    /**
     * 동일 상품 중복 방지
     */
    private boolean containsSameCourse(
            final List<JobCourseVO> courses,
            final JobCourseVO target
    ) {

        return courses.stream()
                .anyMatch(
                        course ->
                                course
                                        .getCourseName()
                                        .equals(
                                                target.getCourseName()
                                        )
                                        && course
                                        .getDiscountPrice()
                                        .equals(
                                                target.getDiscountPrice()
                                        )
                );
    }

    /**
     * Jsoup 연결 공통 처리
     */
    private Document connect(
            final String url
    ) throws IOException {

        return Jsoup
                .connect(url)
                .userAgent(
                        "Mozilla/5.0"
                )
                .timeout(
                        TIMEOUT_MILLIS
                )
                .get();
    }
}

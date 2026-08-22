package org.scoula.job.client;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scoula.job.domain.JobCourseVO;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class GongdangiCourseCrawler {

    private static final String PUBLIC_COURSE_URL =
            "https://gong.conects.com/freepass/renewal/9th";

    private static final String TECHNICAL_COURSE_URL =
            "https://tech.conects.com/freepass/renewal/technical";

    private static final String POLICE_COURSE_URL =
            "https://police.criminallaw100.com/event/short_pass.asp";

    private static final String FIRE_COURSE_URL =
            "https://sobang.conects.com/freepass/renewal";

    private static final String COURSE_TYPE = "C02";
    private static final String PROVIDER_NAME = "공단기";
    private static final int TIMEOUT_MILLIS = 10_000;

    public List<JobCourseVO> crawlCourses() throws IOException {
        final List<JobCourseVO> courses = new ArrayList<>();

        courses.addAll(this.crawlPublicCourses());
        courses.addAll(this.crawlTechnicalCourses());
        courses.addAll(this.crawlPoliceCourses());
        courses.addAll(this.crawlFireCourses());

        return courses;
    }

    /**
     * 일반 9급 / 군무원 상품
     */
    private List<JobCourseVO> crawlPublicCourses()
            throws IOException {

        final Document document =
                this.connect(PUBLIC_COURSE_URL);

        final Elements courseElements =
                document.select(".fp__flex--sb");

        if (courseElements.isEmpty()) {
            throw new IOException(
                    "공단기 일반 상품 영역을 찾을 수 없습니다."
            );
        }

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element courseElement : courseElements) {
            final String courseName = courseElement
                    .select(".item__name")
                    .text()
                    .trim();

            if (!this.isPublicTargetCourse(courseName)) {
                continue;
            }

            final JobCourseVO course =
                    this.createCommonCourse(
                            courseElement,
                            courseName,
                            PUBLIC_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                log.warn(
                        "공단기 일반 상품 파싱 실패: {}",
                        courseName
                );
                continue;
            }

            courses.add(course);
        }

        return courses;
    }

    /**
     * 기술직 상품
     * 전산직 추천 후보로 사용
     */
    private List<JobCourseVO> crawlTechnicalCourses()
            throws IOException {

        final Document document =
                this.connect(TECHNICAL_COURSE_URL);

        final Elements courseElements =
                document.select(".fp__flex--sb");

        if (courseElements.isEmpty()) {
            throw new IOException(
                    "공단기 기술직 상품 영역을 찾을 수 없습니다."
            );
        }

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element courseElement : courseElements) {
            final String courseName = courseElement
                    .select(".item__name")
                    .text()
                    .trim();

            if (!this.isTechnicalTargetCourse(courseName)) {
                continue;
            }

            final JobCourseVO course =
                    this.createCommonCourse(
                            courseElement,
                            courseName,
                            TECHNICAL_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                log.warn(
                        "공단기 기술직 상품 파싱 실패: {}",
                        courseName
                );
                continue;
            }

            courses.add(course);
        }

        return courses;
    }

    /**
     * 경찰 상품
     * 일반형 제외, 환급/갱신형만 수집
     */
    private List<JobCourseVO> crawlPoliceCourses()
            throws IOException {

        final Document document =
                this.connect(POLICE_COURSE_URL);

        final Elements courseElements =
                document.select(".sec6_product li");

        if (courseElements.isEmpty()) {
            throw new IOException(
                    "경찰단기 상품 영역을 찾을 수 없습니다."
            );
        }

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element courseElement : courseElements) {
            final String baseCourseName = courseElement
                    .select("h2")
                    .text()
                    .trim();

            if (baseCourseName.isBlank()) {
                continue;
            }

            final String optionName =
                    this.findPoliceOptionName(
                            courseElement
                    );

            if (!this.isPoliceTargetCourse(optionName)) {
                continue;
            }

            final String originalPriceText = courseElement
                    .select(".sec6_price1 em")
                    .text()
                    .trim();

            final String discountPriceText = courseElement
                    .select(".sec6_price2 em")
                    .text()
                    .trim();

            final String courseName =
                    baseCourseName
                            + " - "
                            + optionName;

            final JobCourseVO courseVO =
                    new JobCourseVO();

            courseVO.setCourseType(COURSE_TYPE);
            courseVO.setProviderName(PROVIDER_NAME);
            courseVO.setCourseName(courseName);

            courseVO.setOriginalPrice(
                    this.parseManwonPrice(
                            originalPriceText
                    )
            );

            courseVO.setDiscountPrice(
                    this.parseManwonPrice(
                            discountPriceText
                    )
            );

            courseVO.setMilitaryPrice(null);
            courseVO.setSelectedCost(null);
            courseVO.setDetailUrl(
                    POLICE_COURSE_URL
            );

            if (!this.isValidCourse(courseVO)) {
                log.warn(
                        "경찰단기 상품 파싱 실패: {}",
                        courseName
                );
                continue;
            }

            courses.add(courseVO);
        }

        return courses;
    }

    /**
     * 소방 공경채 상품
     */
    private List<JobCourseVO> crawlFireCourses()
            throws IOException {

        final Document document =
                this.connect(FIRE_COURSE_URL);

        final Elements courseElements =
                document.select(".list__item");

        if (courseElements.isEmpty()) {
            throw new IOException(
                    "소방단기 상품 영역을 찾을 수 없습니다."
            );
        }

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element courseElement : courseElements) {
            final String courseName = courseElement
                    .select(".item__name")
                    .text()
                    .trim();

            if (!this.isFireTargetCourse(courseName)) {
                continue;
            }

            final JobCourseVO course =
                    this.createCommonCourse(
                            courseElement,
                            courseName,
                            FIRE_COURSE_URL
                    );

            if (!this.isValidCourse(course)) {
                log.warn(
                        "소방단기 상품 파싱 실패: {}",
                        courseName
                );
                continue;
            }

            courses.add(course);
        }

        return courses;
    }

    private JobCourseVO createCommonCourse(
            final Element courseElement,
            final String courseName,
            final String courseUrl
    ) {
        final Long originalPrice = this.parsePrice(
                courseElement
                        .select(".price__after")
                        .text()
        );

        final Long discountPrice = this.parsePrice(
                courseElement
                        .select(".price__coupon")
                        .text()
        );

        final JobCourseVO courseVO =
                new JobCourseVO();

        courseVO.setCourseType(COURSE_TYPE);
        courseVO.setProviderName(PROVIDER_NAME);
        courseVO.setCourseName(courseName);
        courseVO.setOriginalPrice(originalPrice);
        courseVO.setDiscountPrice(discountPrice);
        courseVO.setMilitaryPrice(null);
        courseVO.setSelectedCost(null);
        courseVO.setDetailUrl(courseUrl);

        return courseVO;
    }

    /**
     * 일반 페이지에서 서비스 추천 대상만 수집
     *
     * 포함:
     * - 일반 9급 프리미엄
     * - 9급 군무원
     *
     * 제외:
     * - 7급 / 9·7급
     * - 법원등기직
     * - PSAT
     * - 평생패스
     */
    private boolean isPublicTargetCourse(
            final String courseName
    ) {
        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        if (courseName.contains("평생")
                || courseName.contains("7급")
                || courseName.contains("법원등기")
                || courseName.contains("PSAT")) {
            return false;
        }

        return courseName.contains("9급")
                && (
                courseName.contains("프리미엄")
                        || courseName.contains("군무원")
        );
    }

    /**
     * 기술직 대표 패스만 수집
     */
    private boolean isTechnicalTargetCourse(
            final String courseName
    ) {
        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains("기술직")
                && courseName.contains("프리미엄")
                && !courseName.contains("평생");
    }

    /**
     * 경찰은 환급/갱신형만 추천 후보로 사용
     */
    private boolean isPoliceTargetCourse(
            final String optionName
    ) {
        return "환급/갱신형".equals(optionName);
    }

    /**
     * 소방은 공채/경채 통합 상품만 사용
     */
    private boolean isFireTargetCourse(
            final String courseName
    ) {
        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains("공경채")
                && courseName.contains("환급")
                && !courseName.contains("평생");
    }

    private String findPoliceOptionName(
            final Element courseElement
    ) {
        return courseElement
                .select("span")
                .stream()
                .map(Element::text)
                .filter(text ->
                        "환급/갱신형".equals(text)
                                || "일반형".equals(text)
                )
                .findFirst()
                .orElse("");
    }

    private boolean isValidCourse(
            final JobCourseVO course
    ) {
        return course.getCourseName() != null
                && !course.getCourseName().isBlank()
                && course.getOriginalPrice() != null
                && course.getOriginalPrice() > 0
                && course.getDiscountPrice() != null
                && course.getDiscountPrice() > 0;
    }

    private Document connect(
            final String url
    ) throws IOException {

        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(TIMEOUT_MILLIS)
                .get();
    }

    private Long parsePrice(
            final String price
    ) {
        final String number =
                price.replaceAll(
                        "[^0-9]",
                        ""
                );

        return number.isEmpty()
                ? 0L
                : Long.parseLong(number);
    }

    /**
     * 경찰 가격 예:
     * 150만 -> 1,500,000
     * 73.2만 -> 732,000
     */
    private Long parseManwonPrice(
            final String price
    ) {
        if (price == null
                || price.isBlank()) {
            return 0L;
        }

        final String number = price
                .replace("만", "")
                .replace(",", "")
                .trim();

        if (number.isEmpty()) {
            return 0L;
        }

        return new BigDecimal(number)
                .multiply(
                        BigDecimal.valueOf(10_000)
                )
                .longValue();
    }
}

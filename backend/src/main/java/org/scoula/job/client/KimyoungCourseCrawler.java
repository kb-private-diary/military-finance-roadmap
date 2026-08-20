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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Log4j2
public class KimyoungCourseCrawler {

    private static final String COURSE_URL =
            "https://www.kimyoung.co.kr/freepass/2026/wonder_pass.asp";

    private static final String COURSE_TYPE = "C03";
    private static final String PROVIDER_NAME = "김영편입";

    private static final int TIMEOUT_MILLIS = 10_000;

    private static final Pattern MANWON_PATTERN =
            Pattern.compile("(\\d+)\\s*만원");

    /**
     * 김영편입 대표 패스 수집
     *
     * - 인문 김영패스 원더
     * - 자연 김영패스 원더
     *
     * LITE 상품 제외
     */
    public List<JobCourseVO> crawlCourses()
            throws IOException {

        final Document document =
                this.connect(COURSE_URL);

        final List<JobCourseVO> courses =
                new ArrayList<>();

        courses.addAll(
                this.crawlHumanCourse(document)
        );

        courses.addAll(
                this.crawlMathCourse(document)
        );

        return courses;
    }

    /**
     * 인문계 원더
     */
    private List<JobCourseVO> crawlHumanCourse(
            final Document document
    ) {

        final Elements productElements =
                document.select(
                        ".pass_box.human:not(.lite)"
                );

        return this.createCourses(
                productElements,
                "인문"
        );
    }

    /**
     * 자연계 원더
     */
    private List<JobCourseVO> crawlMathCourse(
            final Document document
    ) {

        final Elements productElements =
                document.select(
                        ".pass_box.math:not(.lite)"
                );

        return this.createCourses(
                productElements,
                "자연"
        );
    }

    private List<JobCourseVO> createCourses(
            final Elements productElements,
            final String division
    ) {

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element productElement :
                productElements) {

            final Element titleElement =
                    productElement.selectFirst(
                            ".txt_wrap h2"
                    );

            final Element divisionElement =
                    productElement.selectFirst(
                            ".txt_wrap em"
                    );

            final Element originalPriceElement =
                    productElement.selectFirst(
                            ".price_origin .po_txt01"
                    );

            final Element discountPriceElement =
                    productElement.selectFirst(
                            ".price_origin .po_txt02"
                    );

            if (titleElement == null
                    || originalPriceElement == null
                    || discountPriceElement == null) {
                continue;
            }

            final String actualDivision =
                    divisionElement != null
                            ? divisionElement.text().trim()
                            : division;

            if (!actualDivision.contains(division)) {
                continue;
            }

            final String baseCourseName =
                    titleElement
                            .text()
                            .trim();

            /*
             * 제목과 계열을 합쳐서 저장.
             *
             * 예)
             * 2027+2028 김영패스 원더 인문
             * 2027+2028 김영패스 원더 자연
             */
            final String courseName =
                    baseCourseName
                            + " "
                            + division;

            final JobCourseVO course =
                    this.createCourse(
                            courseName,
                            originalPriceElement.text(),
                            discountPriceElement.text()
                    );

            if (this.isValidCourse(course)
                    && !this.containsSameCourse(
                    courses,
                    course
            )) {
                courses.add(course);
            }
        }

        return courses;
    }

    private JobCourseVO createCourse(
            final String courseName,
            final String originalPriceText,
            final String discountPriceText
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
                COURSE_URL
        );

        return courseVO;
    }

    /**
     * 157만원 → 1,570,000원
     */
    private Long parseManwonPrice(
            final String price
    ) {

        if (price == null
                || price.isBlank()) {
            return 0L;
        }

        final Matcher matcher =
                MANWON_PATTERN.matcher(price);

        if (matcher.find()) {

            return Long.parseLong(
                    matcher.group(1)
            ) * 10_000L;
        }

        return 0L;
    }

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
                );
    }

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

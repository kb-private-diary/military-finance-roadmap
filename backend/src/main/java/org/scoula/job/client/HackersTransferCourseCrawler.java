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
public class HackersTransferCourseCrawler {

    private static final String COURSE_URL =
            "https://ingang.hackersut.com/event/47324122?_C_=725210";

    private static final String COURSE_TYPE = "C03";
    private static final String PROVIDER_NAME = "해커스편입";

    private static final int TIMEOUT_MILLIS = 10_000;

    private static final Pattern MANWON_PATTERN =
            Pattern.compile("(\\d+)\\s*만원");

    /**
     * 해커스편입 인강 수집
     *
     * - 인문계 최대 400% 환급반
     * - 자연계 최대 400% 환급반
     * - 인문계 편입반
     * - 자연계 편입반
     */
    public List<JobCourseVO> crawlCourses()
            throws IOException {

        final Document document =
                this.connect(COURSE_URL);

        final Elements productElements =
                document.select(
                        ".lect_list > li"
                );

        final List<JobCourseVO> courses =
                new ArrayList<>();

        for (final Element productElement :
                productElements) {

            final Element inputElement =
                    productElement.selectFirst(
                            "input[name=\"lect\"]"
                    );

            final Element imageElement =
                    productElement.selectFirst(
                            "label img[alt]"
                    );

            final Element originalPriceElement =
                    productElement.selectFirst(
                            ".price_area .price"
                    );

            final Element discountPriceElement =
                    productElement.selectFirst(
                            ".price_area .dc_price"
                    );

            if (inputElement == null
                    || imageElement == null
                    || originalPriceElement == null
                    || discountPriceElement == null) {
                continue;
            }

            final String courseName =
                    imageElement
                            .attr("alt")
                            .trim();

            if (!this.isTargetCourse(courseName)) {
                continue;
            }

            final String externalCode =
                    inputElement
                            .attr("data-code")
                            .trim();

            final JobCourseVO course =
                    this.createCourse(
                            courseName,
                            originalPriceElement.text(),
                            discountPriceElement.text(),
                            externalCode
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

    private boolean isTargetCourse(
            final String courseName
    ) {

        if (courseName == null
                || courseName.isBlank()) {
            return false;
        }

        return courseName.contains("인문계")
                || courseName.contains("자연계");
    }

    private JobCourseVO createCourse(
            final String courseName,
            final String originalPriceText,
            final String discountPriceText,
            final String externalCode
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

        /*
         * JobCourseVO에 externalCode 필드가 현재 없다면
         * 여기서는 저장하지 않는다.
         *
         * data-code는 나중에 DB 식별값으로 쓸 수 있으므로
         * 디버그 로그로만 남긴다.
         */
        log.debug(
                "해커스편입 상품 코드: {} / {}",
                externalCode,
                courseName
        );

        return courseVO;
    }

    /**
     * 129만원 → 1,290,000원
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

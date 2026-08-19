package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.job.client.GongdangiCourseCrawler;
import org.scoula.job.client.HackersCertificateCourseCrawler;
import org.scoula.job.client.HackersCourseCrawler;
import org.scoula.job.client.HackersLanguageCourseCrawler;
import org.scoula.job.client.HackersTransferCourseCrawler;
import org.scoula.job.client.InflearnCrawlerClient;
import org.scoula.job.client.KimyoungCourseCrawler;
import org.scoula.job.domain.JobCourseVO;
import org.scoula.job.domain.JobQualificationVO;
import org.scoula.job.dto.JobCourseCrawlDTO;
import org.scoula.job.mapper.JobMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobCourseSyncService {

    private static final String SYNC_USER =
            "COURSE_SYNC";

    private static final String QUAL_TYPE_CERTIFICATE =
            "Q01";

    private static final String QUAL_TYPE_LANGUAGE =
            "Q02";

    /*
     * 공무원 인강 매핑 코드
     */
    private static final String COURSE_MAPPING_GENERAL =
            "GENERAL";

    private static final String COURSE_MAPPING_GENERAL_IT =
            "GENERAL_IT";

    private static final String COURSE_MAPPING_MILITARY =
            "MILITARY";

    private static final String COURSE_MAPPING_MILITARY_IT =
            "MILITARY_IT";

    private static final String COURSE_MAPPING_POLICE =
            "POLICE";

    private static final String COURSE_MAPPING_FIRE =
            "FIRE";

    private static final String COURSE_MAPPING_HUMAN =
            "HUMAN";

    private static final String COURSE_MAPPING_NATURAL =
            "NATURAL";

    private final JobMapper jobMapper;

    private final GongdangiCourseCrawler gongdangiCourseCrawler;

    private final HackersCourseCrawler hackersCourseCrawler;

    private final HackersCertificateCourseCrawler
            hackersCertificateCourseCrawler;

    private final HackersLanguageCourseCrawler
            hackersLanguageCourseCrawler;

    private final HackersTransferCourseCrawler
            hackersTransferCourseCrawler;

    private final InflearnCrawlerClient inflearnCrawlerClient;

    private final KimyoungCourseCrawler kimyoungCourseCrawler;

    // =========================================================
    // 전체 동기화
    // =========================================================

    public void syncAllCourses() {

        log.info("전체 인강 동기화 시작");

        this.syncCertificateCourses();
        this.syncLanguageCourses();
        this.syncPublicServiceCourses();
        this.syncTransferCourses();

        log.info("전체 인강 동기화 완료");
    }

    // =========================================================
    // C01 - 자격증
    // =========================================================
    public void syncCertificateCourses() {

        log.info("자격증 인강 동기화 시작");

        final List<JobQualificationVO> qualifications =
                this.jobMapper.findQualificationListByQualType(
                        QUAL_TYPE_CERTIFICATE
                );

        /*
         * 인프런은 하나의 ChromeDriver로
         * 모든 자격증을 연속 크롤링한다.
         */
        final List<String> qualificationNames =
                qualifications.stream()
                        .map(
                                JobQualificationVO::getQualName
                        )
                        .toList();

        final Map<String, List<JobCourseCrawlDTO>>
                inflearnCourseMap =
                this.inflearnCrawlerClient
                        .findCoursesBatch(
                                qualificationNames
                        );

        for (final JobQualificationVO qualification
                : qualifications) {

            final Long qualId =
                    qualification.getQualId();

            final String qualName =
                    qualification.getQualName();

            /*
             * 해커스 자격증
             */
            try {

                final List<JobCourseVO> hackersCourses =
                        this.hackersCertificateCourseCrawler
                                .crawlCertificateCourses(
                                        qualName
                                );

                this.saveQualificationCourses(
                        qualId,
                        hackersCourses
                );

            } catch (Exception e) {

                log.warn(
                        "해커스 자격증 인강 동기화 실패"
                                + " - qualId: {}, qualName: {}",
                        qualId,
                        qualName,
                        e
                );
            }

            /*
             * 인프런
             */
            try {

                final List<JobCourseCrawlDTO> inflearnCourses =
                        inflearnCourseMap.getOrDefault(
                                qualName,
                                List.of()
                        );

                final List<JobCourseVO> convertedCourses =
                        inflearnCourses.stream()
                                .map(
                                        this::convertInflearnCourse
                                )
                                .toList();

                this.saveQualificationCourses(
                        qualId,
                        convertedCourses
                );

            } catch (Exception e) {

                log.warn(
                        "인프런 인강 동기화 실패"
                                + " - qualId: {}, qualName: {}",
                        qualId,
                        qualName,
                        e
                );
            }
        }

        log.info("자격증 인강 동기화 완료");
    }

    // =========================================================
    // C01 - 어학
    // =========================================================

    public void syncLanguageCourses() {

        log.info("어학 인강 동기화 시작");

        final List<JobQualificationVO> languages =
                this.jobMapper.findQualificationListByQualType(
                        QUAL_TYPE_LANGUAGE
                );

        for (final JobQualificationVO language
                : languages) {

            final Long qualId =
                    language.getQualId();

            final String qualName =
                    language.getQualName();

            try {

                final List<JobCourseVO> courses =
                        this.findLanguageCourses(
                                qualName
                        );

                this.saveQualificationCourses(
                        qualId,
                        courses
                );

            } catch (Exception e) {

                log.warn(
                        "해커스 어학 인강 동기화 실패"
                                + " - qualId: {}, qualName: {}",
                        qualId,
                        qualName,
                        e
                );
            }
        }

        log.info("어학 인강 동기화 완료");
    }

    // =========================================================
    // C02 - 공무원
    // =========================================================

    public void syncPublicServiceCourses() {

        log.info("공무원 인강 동기화 시작");

        /*
         * 해커스 공무원
         */
        try {

            final List<JobCourseVO> hackersCourses =
                    this.hackersCourseCrawler
                            .crawlCourses();

            this.savePublicServiceCourses(
                    hackersCourses
            );

        } catch (Exception e) {

            log.warn(
                    "해커스 공무원 인강 동기화 실패",
                    e
            );
        }

        /*
         * 공단기
         */
        try {

            final List<JobCourseVO> gongdangiCourses =
                    this.gongdangiCourseCrawler
                            .crawlCourses();

            this.savePublicServiceCourses(
                    gongdangiCourses
            );

        } catch (Exception e) {

            log.warn(
                    "공단기 인강 동기화 실패",
                    e
            );
        }

        log.info("공무원 인강 동기화 완료");
    }

    // =========================================================
    // C03 - 편입
    // =========================================================

    public void syncTransferCourses() {

        log.info("편입 인강 동기화 시작");

        /*
         * 해커스 편입
         */
        try {

            final List<JobCourseVO> hackersCourses =
                    this.hackersTransferCourseCrawler
                            .crawlCourses();

            this.saveTransferCourses(
                    hackersCourses
            );

        } catch (Exception e) {

            log.warn(
                    "해커스 편입 인강 동기화 실패",
                    e
            );
        }

        /*
         * 김영편입
         */
        try {

            final List<JobCourseVO> kimyoungCourses =
                    this.kimyoungCourseCrawler
                            .crawlCourses();

            this.saveTransferCourses(
                    kimyoungCourses
            );

        } catch (Exception e) {

            log.warn(
                    "김영편입 인강 동기화 실패",
                    e
            );
        }

        log.info("편입 인강 동기화 완료");
    }

    // =========================================================
    // 어학 크롤러 분기
    // =========================================================

    private List<JobCourseVO> findLanguageCourses(
            final String qualName
    ) {

        if (qualName == null
                || qualName.isBlank()) {

            return List.of();
        }

        final String normalizedName =
                qualName
                        .replaceAll("\\s+", "")
                        .toUpperCase();

        /*
         * TOEIC
         */
        if ("TOEIC".equals(normalizedName)
                || "토익".equals(
                qualName
        )) {

            return this.hackersLanguageCourseCrawler
                    .crawlToeicCourses();
        }

        /*
         * TOEIC Speaking
         */
        if (normalizedName.contains(
                "TOEICSPEAKING"
        )
                || qualName.contains(
                "토익스피킹"
        )
                || qualName.contains(
                "토익 스피킹"
        )) {

            return this.hackersLanguageCourseCrawler
                    .crawlToeicSpeakingCourses();
        }

        /*
         * OPIc
         */
        if (normalizedName.contains(
                "OPIC"
        )
                || qualName.contains(
                "오픽"
        )) {

            return this.hackersLanguageCourseCrawler
                    .crawlOpicCourses();
        }

        /*
         * HSK
         */
        if (normalizedName.contains(
                "HSK5급"
        )
                || normalizedName.contains(
                "HSK6급"
        )) {

            return this.hackersLanguageCourseCrawler
                    .crawlHskCourses(
                            qualName
                    );
        }

        /*
         * JLPT
         */
        if (normalizedName.contains(
                "JLPTN1"
        )
                || normalizedName.contains(
                "JLPTN2"
        )) {

            return this.hackersLanguageCourseCrawler
                    .crawlJlptCourses(
                            qualName
                    );
        }

        log.info(
                "해커스 어학 크롤링 미지원"
                        + " - qualName: {}",
                qualName
        );

        return List.of();
    }

    // =========================================================
    // 자격증·어학 저장
    // =========================================================

    private void saveQualificationCourses(
            final Long qualId,
            final List<JobCourseVO> courses
    ) {

        if (courses == null
                || courses.isEmpty()) {

            return;
        }

        for (final JobCourseVO course
                : courses) {

            final Long courseId =
                    this.saveCourse(
                            course
                    );

            this.jobMapper
                    .upsertQualificationCourse(
                            qualId,
                            courseId,
                            SYNC_USER
                    );
        }
    }

    // =========================================================
    // 공무원 저장
    // =========================================================

    private void savePublicServiceCourses(
            final List<JobCourseVO> courses
    ) {

        if (courses == null
                || courses.isEmpty()) {

            return;
        }

        for (final JobCourseVO course
                : courses) {

            final Long courseId =
                    this.saveCourse(
                            course
                    );

            final List<Long> categoryIds =
                    this.resolvePublicServiceCategoryIds(
                            course
                    );

            for (final Long categoryId
                    : categoryIds) {

                this.jobMapper
                        .upsertCategoryCourse(
                                categoryId,
                                courseId,
                                SYNC_USER
                        );
            }
        }
    }

    /**
     * 공무원 강의명을 기준으로
     * 어떤 직렬에 연결할지 결정한다.
     */
    private List<Long> resolvePublicServiceCategoryIds(
            final JobCourseVO course
    ) {

        final String courseName =
                course.getCourseName();

        if (courseName == null
                || courseName.isBlank()) {

            return List.of();
        }

        /*
         * 군무원
         */
        if (courseName.contains("군무원")) {

            /*
             * 군무원 전산직
             */
            if (courseName.contains("전산")) {

                final Long categoryId =
                        this.jobMapper
                                .findCategoryIdByCourseMappingCode(
                                        COURSE_MAPPING_MILITARY_IT
                                );

                return categoryId == null
                        ? List.of()
                        : List.of(categoryId);
            }

            /*
             * 군무원 공통 패스
             * 전산직 제외
             */
            final List<Long> categoryIds =
                    this.jobMapper
                            .findChildCategoryIdsByCourseMappingCode(
                                    COURSE_MAPPING_MILITARY
                            );

            final Long computerCategoryId =
                    this.jobMapper
                            .findCategoryIdByCourseMappingCode(
                                    COURSE_MAPPING_MILITARY_IT
                            );

            return categoryIds.stream()
                    .filter(categoryId ->
                            !categoryId.equals(computerCategoryId)
                    )
                    .toList();
        }

        /*
         * 경찰
         */
        if (courseName.contains("경찰")
                || courseName.contains("순경")) {

            return this.jobMapper
                    .findChildCategoryIdsByCourseMappingCode(
                            COURSE_MAPPING_POLICE
                    );
        }

        /*
         * 소방
         */
        if (courseName.contains("소방")
                || courseName.contains("공경채")) {

            return this.jobMapper
                    .findChildCategoryIdsByCourseMappingCode(
                            COURSE_MAPPING_FIRE
                    );
        }

        /*
         * 일반직 전산
         */
        if (courseName.contains("전산직")
                || courseName.contains("전산")) {

            final Long categoryId =
                    this.jobMapper
                            .findCategoryIdByCourseMappingCode(
                                    COURSE_MAPPING_GENERAL_IT
                            );

            return categoryId == null
                    ? List.of()
                    : List.of(categoryId);
        }

        /*
         * 일반직 9급 공통 패스
         * 전산 제외
         */
        if (courseName.contains("9급")) {

            final List<Long> categoryIds =
                    this.jobMapper
                            .findChildCategoryIdsByCourseMappingCode(
                                    COURSE_MAPPING_GENERAL
                            );

            final Long computerCategoryId =
                    this.jobMapper
                            .findCategoryIdByCourseMappingCode(
                                    COURSE_MAPPING_GENERAL_IT
                            );

            return categoryIds.stream()
                    .filter(categoryId ->
                            !categoryId.equals(computerCategoryId)
                    )
                    .toList();
        }

        log.info(
                "공무원 인강 매핑 대상 없음"
                        + " - courseName: {}",
                courseName
        );

        return List.of();
    }

    // =========================================================
    // 편입 저장
    // =========================================================

    private void saveTransferCourses(
            final List<JobCourseVO> courses
    ) {

        if (courses == null
                || courses.isEmpty()) {

            return;
        }

        for (final JobCourseVO course
                : courses) {

            final Long courseId =
                    this.saveCourse(
                            course
                    );

            final List<String> majorCodes =
                    this.resolveTransferMajorCodes(
                            course
                    );

            for (final String majorCode
                    : majorCodes) {

                this.jobMapper
                        .upsertTransferMajorCourse(
                                majorCode,
                                courseId,
                                SYNC_USER
                        );
            }
        }
    }

    private List<String> resolveTransferMajorCodes(
            final JobCourseVO course
    ) {

        final String courseName =
                course.getCourseName();

        if (courseName == null
                || courseName.isBlank()) {

            return List.of();
        }

        /*
         * 인문계 강의
         */
        if (courseName.contains(
                "인문"
        )) {

            return this.jobMapper
                    .findTransferMajorCodesByCourseMappingCode(
                            COURSE_MAPPING_HUMAN
                    );
        }

        /*
         * 자연계·이공계 강의
         */
        if (courseName.contains(
                "자연"
        )
                || courseName.contains(
                "이공"
        )) {

            return this.jobMapper
                    .findTransferMajorCodesByCourseMappingCode(
                            COURSE_MAPPING_NATURAL
                    );
        }

        /*
         * 인문/자연 구분이 없는 공통 편입 강의
         */
        return this.jobMapper
                .findAllTransferMajorCodes();
    }

    // =========================================================
    // 인프런 DTO → JobCourseVO
    // =========================================================

    private JobCourseVO convertInflearnCourse(
            final JobCourseCrawlDTO crawlDTO
    ) {

        final JobCourseVO course =
                new JobCourseVO();

        course.setCourseType(
                "C01"
        );

        course.setProviderName(
                crawlDTO.getProviderName()
        );

        course.setCourseName(
                crawlDTO.getCourseName()
        );

        course.setOriginalPrice(
                crawlDTO.getOriginalPrice()
        );

        course.setDiscountPrice(
                crawlDTO.getDiscountPrice()
        );

        course.setMilitaryPrice(
                null
        );

        course.setSelectedCost(
                null
        );

        course.setBenefitDetail(
                null
        );

        course.setDetailUrl(
                crawlDTO.getDetailUrl()
        );

        return course;
    }

    // =========================================================
    // job_course 저장 / 갱신
    // =========================================================

    private Long saveCourse(
            final JobCourseVO course
    ) {

        Long courseId =
                this.jobMapper
                        .findCourseIdByProviderAndName(
                                course.getProviderName(),
                                course.getCourseName()
                        );

        /*
         * 신규 강의
         */
        if (courseId == null) {

            course.setCreatedNm(
                    SYNC_USER
            );

            this.jobMapper
                    .insertJobCourse(
                            course
                    );

            courseId =
                    course.getCourseId();

            log.info(
                    "인강 신규 저장"
                            + " - courseId: {}, provider: {}, courseName: {}",
                    courseId,
                    course.getProviderName(),
                    course.getCourseName()
            );

            return courseId;
        }

        /*
         * 기존 강의 갱신
         */
        course.setCourseId(
                courseId
        );

        course.setModifiedNm(
                SYNC_USER
        );

        this.jobMapper
                .updateJobCourse(
                        course
                );

        log.info(
                "인강 갱신"
                        + " - courseId: {}, provider: {}, courseName: {}",
                courseId,
                course.getProviderName(),
                course.getCourseName()
        );

        return courseId;
    }
}

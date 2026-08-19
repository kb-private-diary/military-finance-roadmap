package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.scoula.job.client.QnetApiClient;
import org.scoula.job.client.Work24ApiClient;
import org.scoula.job.domain.JobCategoryVO;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobTrainingVO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.JobProductDTO;
import org.scoula.job.dto.JobTrainingDTO;
import org.scoula.job.dto.JobTransferMajorDTO;
import org.scoula.job.dto.JobTransferUniversityDTO;
import org.scoula.job.domain.JobQualificationVO;
import org.scoula.job.domain.JobCourseVO;
import org.scoula.job.domain.JobRecommendServiceVO;
import org.scoula.job.dto.JobCategoryDTO;
import org.scoula.job.dto.JobQualificationDTO;
import org.scoula.job.dto.JobCourseDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
import org.scoula.job.dto.ServiceRecommendResponseDTO;
import org.scoula.job.mapper.JobMapper;
import org.scoula.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobServiceImpl implements JobService {

    private static final String GOAL_TYPE_EMPLOYMENT = "J01";
    private static final String GOAL_TYPE_PUBLIC_SERVICE = "J02";
    private static final String GOAL_TYPE_TRANSFER = "J03";

    private static final int ROADMAP_CATEGORY_JOB = 2;

    /*
     * NCS 코드를 세분류(4차) → 소분류(3차) → 중분류(2차) 순으로 넓혀가며 조회한다.
     * 대분류(1차)까지 넓히면 직무와 무관한 과정이 섞이므로 중분류에서 멈춘다.
     */
    private static final int[] NCS_SEARCH_DEPTHS = { 4, 3, 2 };
    private final JobMapper jobMapper;
    private final ProductService productService;
    private final Work24ApiClient work24ApiClient;

    @Override
    @Transactional(readOnly = true)
    public List<JobCategoryDTO> findCategoryList(String goalType) {
        return this.jobMapper.findCategoryListByGoalType(goalType)
                .stream()
                .map(JobCategoryDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTransferUniversityDTO> findTransferUniversityList() {
        return this.jobMapper.findTransferUniversityList()
                .stream()
                .map(JobTransferUniversityDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobTransferMajorDTO> findTransferMajorList(Long univId) {
        return this.jobMapper.findTransferMajorListByUnivId(univId)
                .stream()
                .map(JobTransferMajorDTO::of)
                .toList();
    }

    // 목표 등록
    @Override
    @Transactional
    public JobGoalCreateResponseDTO createJobGoal(
            Long userId,
            String username,
            JobGoalCreateRequestDTO requestDTO) {
        // 목표 유형별 필수 입력값 검증
        if (GOAL_TYPE_EMPLOYMENT.equals(requestDTO.getGoalType())
                || GOAL_TYPE_PUBLIC_SERVICE.equals(requestDTO.getGoalType())) {

            // 취업·공무원은 직무·직렬 선택 필수
            if (requestDTO.getCategoryId() == null) {
                throw BusinessException.badRequest(
                        "직무·직렬을 선택해주세요",
                        "JOB_005"
                );
            }

        } else if (GOAL_TYPE_TRANSFER.equals(requestDTO.getGoalType())) {

            // 편입은 대학교 선택 필수
            if (requestDTO.getUnivId() == null) {
                throw BusinessException.badRequest(
                        "대학교를 선택해주세요",
                        "JOB_006"
                );
            }

            // 편입은 학과·계열 선택 필수
            if (requestDTO.getMajorId() == null) {
                throw BusinessException.badRequest(
                        "학과·계열을 선택해주세요",
                        "JOB_007"
                );
            }

        } else {
            // J01, J02, J03 이외의 목표 유형
            throw BusinessException.badRequest(
                    "올바르지 않은 목표유형입니다",
                    "JOB_004"
            );
        }

        JobGoalVO jobGoalVO = new JobGoalVO();
        jobGoalVO.setUserId(userId);
        jobGoalVO.setGoalType(requestDTO.getGoalType());
        jobGoalVO.setCategoryId(requestDTO.getCategoryId());
        jobGoalVO.setUnivId(requestDTO.getUnivId());
        jobGoalVO.setMajorId(requestDTO.getMajorId());
        jobGoalVO.setExpectedDate(requestDTO.getExpectedDate());
        jobGoalVO.setCreatedNm(username);

        this.jobMapper.insertJobGoal(jobGoalVO);

        return new JobGoalCreateResponseDTO(jobGoalVO.getGoalId());
    }

    // 작성 중인 진로 목표 수정
    @Override
    @Transactional
    public void updateJobGoal(
            Long goalId,
            Long userId,
            String username,
            JobGoalCreateRequestDTO requestDTO) {

        // 목표 존재 여부 + 로그인한 사용자의 목표인지 확인
        JobGoalVO jobGoalVO =
                this.findOwnedJobGoalOrThrow(userId, goalId);

        // 작성 중인 DRAFT 목표만 수정 가능
        if (!"DRAFT".equals(jobGoalVO.getStatus())) {
            throw BusinessException.conflict(
                    "작성 중인 진로 목표만 수정할 수 있습니다.",
                    "JOB_008"
            );
        }

        // 목표 유형별 필수 입력값 검증
        if (GOAL_TYPE_EMPLOYMENT.equals(requestDTO.getGoalType())
                || GOAL_TYPE_PUBLIC_SERVICE.equals(requestDTO.getGoalType())) {

            if (requestDTO.getCategoryId() == null) {
                throw BusinessException.badRequest(
                        "직무·직렬을 선택해주세요",
                        "JOB_005"
                );
            }

        } else if (GOAL_TYPE_TRANSFER.equals(requestDTO.getGoalType())) {

            if (requestDTO.getUnivId() == null) {
                throw BusinessException.badRequest(
                        "대학교를 선택해주세요",
                        "JOB_006"
                );
            }

            if (requestDTO.getMajorId() == null) {
                throw BusinessException.badRequest(
                        "학과·계열을 선택해주세요",
                        "JOB_007"
                );
            }

        } else {
            throw BusinessException.badRequest(
                    "올바르지 않은 목표유형입니다",
                    "JOB_004"
            );
        }

        // 기존 goalId는 유지하고 입력값만 수정
        jobGoalVO.setGoalType(requestDTO.getGoalType());
        jobGoalVO.setCategoryId(requestDTO.getCategoryId());
        jobGoalVO.setUnivId(requestDTO.getUnivId());
        jobGoalVO.setMajorId(requestDTO.getMajorId());
        jobGoalVO.setExpectedDate(requestDTO.getExpectedDate());
        jobGoalVO.setModifiedNm(username);

        this.jobMapper.updateJobGoal(jobGoalVO);
    }

    // 준비항목 추천 조회
    @Override
    @Transactional(readOnly = true)
    public PrepItemRecommendResponseDTO findPrepItemRecommend(Long goalId) {
        JobGoalVO jobGoalVO = this.findJobGoalOrThrow(goalId);

        List<JobQualificationVO> qualificationVOList;
        List<JobCourseVO> courseVOList = List.of();

        if (GOAL_TYPE_EMPLOYMENT.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByCategoryId(jobGoalVO.getCategoryId());

            List<Long> qualIds = qualificationVOList.stream().map(JobQualificationVO::getQualId).toList();
            // 매핑된 자격증이 없으면 IN 절이 비어 SQL 문법 오류가 나므로 조회하지 않는다.
                 if (!qualIds.isEmpty()) {
                     courseVOList = this.jobMapper.findCourseListByQualificationIds(qualIds);
                 }

        } else if (GOAL_TYPE_PUBLIC_SERVICE.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByCategoryId(jobGoalVO.getCategoryId());

            courseVOList = this.jobMapper.findCourseListByCategoryId(jobGoalVO.getCategoryId());

        } else if (GOAL_TYPE_TRANSFER.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByMajorId(jobGoalVO.getMajorId());

            courseVOList = this.jobMapper.findCourseListByMajorId(jobGoalVO.getMajorId());

        } else {
            throw BusinessException.badRequest("올바르지 않은 목표유형입니다", "JOB_004");
        }

        // 자격증 기본정보 DTO 변환
        // Q-Net 응시료·시험일정은 스케줄러가 DB에 반영하므로 조회 시 API를 호출하지 않는다.
        List<JobQualificationDTO> qualifications = qualificationVOList.stream()
                .map(JobQualificationDTO::of)
                .toList();


        List<JobCourseDTO> courses = courseVOList.stream().map(JobCourseDTO::of).toList();

        return PrepItemRecommendResponseDTO.builder()
                .goalId(goalId)
                .goalType(jobGoalVO.getGoalType())
                .qualifications(qualifications)
                .courses(courses)
                .build();
    }

    // 선택한 준비항목 저장
    @Override
    @Transactional
    public JobPlanCreateResponseDTO createJobPlans(
            Long goalId,
            Long userId,
            String username,
            JobPlanCreateRequestDTO requestDTO) {
        JobGoalVO jobGoalVO = this.findOwnedJobGoalOrThrow(userId, goalId);

        List<Long> qualIds = requestDTO.getQualIds();
        List<Long> courseIds = requestDTO.getCourseIds();
        List<JobTrainingDTO> trainings = requestDTO.getTrainings();

        boolean hasQualification = qualIds != null && !qualIds.isEmpty();
        boolean hasCourse = courseIds != null && !courseIds.isEmpty();
        boolean hasTraining = trainings != null && !trainings.isEmpty();

        // 자격증·어학 또는 인강을 하나 이상 선택해야 함
        if (!hasQualification && !hasCourse && !hasTraining) {
            throw BusinessException.badRequest("준비항목을 1개 이상 선택해주세요", "JOB_002");
        }

        // 훈련과정은 취업(J01) 목표에서만 선택 가능
        if (hasTraining && !"J01".equals(jobGoalVO.getGoalType())) {
            throw BusinessException.badRequest(
                    "훈련과정은 취업 목표에서만 선택할 수 있습니다",
                    "JOB_009"
            );
        }

        // 선택한 자격증·어학 조회
        List<JobQualificationVO> qualificationVOList = hasQualification
                ? this.jobMapper.findQualificationListByIds(qualIds)
                : List.of();

        // 선택한 인강 조회
        List<JobCourseVO> courseVOList = hasCourse
                ? this.jobMapper.findCourseListByIds(courseIds)
                : List.of();

        // 요청한 자격증·어학 중 존재하지 않는 항목이 있는지 확인
        if (hasQualification && qualificationVOList.size() != qualIds.size()) {
            throw BusinessException.notFound("존재하지 않는 준비항목이 포함되어 있습니다", "JOB_003");
        }

        // 요청한 인강 중 존재하지 않는 항목이 있는지 확인
        if (hasCourse && courseVOList.size() != courseIds.size()) {
            throw BusinessException.notFound("존재하지 않는 준비항목이 포함되어 있습니다", "JOB_003");
        }

        // 선택 당시 준비비용 계산
        // Q-Net 응시료는 스케줄러가 DB에 반영하므로 DB 금액을 그대로 스냅샷으로 저장한다.
        if (hasQualification) {

            for (JobQualificationVO qualification : qualificationVOList) {

                qualification.setSelectedCost(
                        this.calculateQualificationCost(qualification)
                );
            }
        }

        // ─────────────────────────────────────────────
        // 선택한 고용24 훈련과정 → 저장용 VO 변환
        // ─────────────────────────────────────────────
        List<JobTrainingVO> trainingVOList = hasTraining
                ? trainings.stream()
                .map(training -> {
                    JobTrainingVO trainingVO = new JobTrainingVO();

                    trainingVO.setGoalId(goalId);

                    trainingVO.setExternalCode(training.getExternalCode());
                    trainingVO.setTrainingRound(training.getTrainingRound());
                    trainingVO.setInstitutionId(training.getInstitutionId());

                    trainingVO.setTrainingName(training.getTrainingName());
                    trainingVO.setInstitutionName(training.getInstitutionName());
                    trainingVO.setTrainingType(training.getTrainingType());
                    trainingVO.setAddress(training.getAddress());

                    // 전체 훈련비
                    trainingVO.setTrainingCost(training.getTrainingCost());

                    // 본인부담금을 실제 준비비용으로 저장
                    trainingVO.setSelectedCost(training.getSelfPayment());

                    trainingVO.setStartDate(training.getStartDate());
                    trainingVO.setEndDate(training.getEndDate());
                    trainingVO.setDetailUrl(training.getDetailUrl());

                    return trainingVO;
                })
                .toList()
                : List.of();

        // 기존 선택 항목 soft delete 후 새 선택 항목 저장
        this.jobMapper.deleteGoalQualificationByGoalId(goalId, username);
        this.jobMapper.deleteGoalCourseByGoalId(goalId, username);
        this.jobMapper.deleteGoalTrainingByGoalId(goalId, username);


        // 새 선택 자격증·어학 저장
        if (hasQualification) {
            this.jobMapper.insertGoalQualificationList(goalId, qualificationVOList, username);
        }

        // 새 선택 인강 저장
        if (hasCourse) {
            this.jobMapper.insertGoalCourseList(goalId, courseIds, username);
        }

        // 새 선택 훈련과정 저장
        if (hasTraining) {
            this.jobMapper.insertGoalTrainingList(
                    goalId,
                    trainingVOList,
                    username
            );
        }

        // 저장 결과 반환
        return JobPlanCreateResponseDTO.builder()
                .goalId(goalId)
                .qualifications(qualificationVOList.stream().map(JobQualificationDTO::of).toList())
                .courses(courseVOList.stream().map(JobCourseDTO::of).toList())
                .trainings(hasTraining ? trainings : List.of())
                .build();
    }

    // 자격증 기존 DB 금액 계산
    private Long calculateQualificationCost(
            JobQualificationVO qualification) {

        // 군인 전용 금액이 있으면 우선 사용
        if (qualification.getMilitaryFee() != null) {
            return qualification.getMilitaryFee();
        }

        long writtenFee = qualification.getWrittenFee() != null
                ? qualification.getWrittenFee()
                : 0L;

        long practicalFee = qualification.getPracticalFee() != null
                ? qualification.getPracticalFee()
                : 0L;

        return writtenFee + practicalFee;
    }

    // 정책·금융상품 추천 조회
    @Override
    @Transactional(readOnly = true)
    public ServiceRecommendResponseDTO findServiceRecommend(Long goalId) {
        JobGoalVO jobGoalVO = this.findJobGoalOrThrow(goalId);

        List<JobRecommendServiceVO> serviceVOList =
                this.jobMapper.findRecommendServiceListByGoalType(jobGoalVO.getGoalType());

        List<JobProductDTO> policies = serviceVOList.stream()
                .filter(service -> "P01".equals(service.getServiceType()))
                .map(JobProductDTO::ofService)
                .toList();

        List<JobProductDTO> financialProducts = serviceVOList.stream()
                .filter(service -> "P02".equals(service.getServiceType()))
                .map(JobProductDTO::ofService)
                .collect(Collectors.toCollection(ArrayList::new));

        this.productService.findCardProductListByCategory(ROADMAP_CATEGORY_JOB).stream()
                .map(JobProductDTO::ofCard)
                .forEach(financialProducts::add);

        return ServiceRecommendResponseDTO.builder()
                .goalId(goalId)
                .policies(policies)
                .financialProducts(financialProducts)
                .build();
    }

    // 진로 목표 상세 조회
    @Override
    @Transactional(readOnly = true)
    public JobGoalDetailResponseDTO findJobGoalDetail(Long goalId) {

        // 목표 기본정보 + 직무·직렬명 + 대학명 + 학과계열명 조회
        JobGoalDetailResponseDTO detail =
                this.jobMapper.findJobGoalDetail(goalId);

        if (detail == null) {
            throw BusinessException.notFound(
                    "진로 목표를 찾을 수 없습니다",
                    "JOB_001"
            );
        }

        // 저장된 자격증·어학에 Q-Net 응시료·시험일정 정보 추가
        List<JobQualificationDTO> qualifications =
                this.jobMapper.findSelectedQualificationListByGoalId(goalId)
                        .stream()
                        .map(JobQualificationDTO::of)
                        .toList();

        // 목표에 저장된 인강 조회
        List<JobCourseDTO> courses =
                this.jobMapper.findSelectedCourseListByGoalId(goalId)
                        .stream()
                        .map(JobCourseDTO::of)
                        .toList();

        // 목표에 저장된 훈련과정 조회
        List<JobTrainingDTO> trainings =
                this.jobMapper.findSelectedTrainingListByGoalId(goalId)
                        .stream()
                        .map(JobTrainingDTO::of)
                        .toList();

        // 목표유형에 맞는 정책·KB 서비스 추천 조회
        ServiceRecommendResponseDTO services =
                this.findServiceRecommend(goalId);

        detail.setQualifications(qualifications);
        detail.setCourses(courses);
        detail.setTrainings(trainings);
        detail.setPolicies(services.getPolicies());
        detail.setFinancialProducts(services.getFinancialProducts());

        return detail;
    }

    // 진로 목표 저장 확정
    @Override
    @Transactional
    public void confirmJobGoal(
            Long goalId,
            Long userId,
            String username) {
        // 로그인한 사용자의 목표인지 확인
        this.findOwnedJobGoalOrThrow(userId, goalId);

        this.jobMapper.updateJobGoalStatus(
                goalId,
                "CONFIRMED",
                username
        );
    }

    // 진로 목표를 조회하고 존재하지 않으면 예외를 던진다
    private JobGoalVO findJobGoalOrThrow(Long goalId) {
        JobGoalVO jobGoalVO = this.jobMapper.findJobGoal(goalId);
        if (jobGoalVO == null) {
            throw BusinessException.notFound("진로 목표를 찾을 수 없습니다", "JOB_001");
        }
        return jobGoalVO;
    }

    // 로그인한 사용자의 진로 목표인지 확인
    private JobGoalVO findOwnedJobGoalOrThrow(
            Long userId,
            Long goalId) {
        JobGoalVO jobGoalVO = this.findJobGoalOrThrow(goalId);
        if (!jobGoalVO.getUserId().equals(userId)) {
            throw BusinessException.forbidden(
                    "해당 요청에 대한 권한이 없습니다.",
                    "AUTH_004"
            );
        }
        return jobGoalVO;
    }

    // 작성 중인 진로 목표 조회
    // 사용자에게 DRAFT 상태의 목표가 있으면 가장 최근 목표를 조회하여
    // 기존 목표 상세 조회 로직을 재사용해 이어쓰기 데이터를 반환
    @Override
    @Transactional(readOnly = true)
    public JobGoalDetailResponseDTO findCurrentJobGoal(Long userId) {

        // 사용자의 가장 최근 DRAFT 목표 ID 조회
        Long goalId = this.jobMapper.findCurrentJobGoalIdByUserId(userId);

        // 작성 중인 목표가 없으면 null 반환
        if (goalId == null) {
            return null;
        }

        // 기존 목표 상세 조회 로직을 재사용하여 이어쓰기 데이터 반환
        return this.findJobGoalDetail(goalId);
    }

    @Override
    public List<JobTrainingDTO> findTrainingRecommend(
            Long goalId,
            String regionCode) {

        // 목표에 연결된 직무 카테고리 조회
        JobCategoryVO category =
                this.jobMapper.findJobCategoryByGoalId(goalId);

        if (category == null) {
            throw BusinessException.notFound(
                    "진로 목표의 직무 정보를 찾을 수 없습니다.",
                    "JOB_014"
            );
        }

        // 고용24 조회에 사용할 NCS 코드
        String ncsCode = category.getNcsCode();

        if (ncsCode == null || ncsCode.isBlank()) {
            throw BusinessException.badRequest(
                    "해당 직무의 훈련과정 추천 정보를 제공할 수 없습니다.",
                    "JOB_015"
            );
        }

        /*
         * IT·개발 대분류(parentId = 4)인 경우
         * K-디지털 트레이닝(C0104)을 우선 조회
         */
        String courseType =
                Long.valueOf(4L).equals(category.getParentId())
                        ? "C0104"
                        : null;

        List<Work24ApiClient.TrainingCourse> courses = List.of();

        for (int ncsDepth : NCS_SEARCH_DEPTHS) {

            courses = this.work24ApiClient.findTrainingCourses(
                    regionCode,
                    ncsCode,
                    courseType,
                    ncsDepth
            );

            if (!courses.isEmpty()) {
                break;
            }

            /*
             * IT 직무인데 K-디지털 과정이 없는 경우
             * 같은 NCS 단계에서 훈련유형 조건만 제거하고 재조회
             */
            if (courseType != null) {

                courses = this.work24ApiClient.findTrainingCourses(
                        regionCode,
                        ncsCode,
                        null,
                        ncsDepth
                );

                if (!courses.isEmpty()) {
                    break;
                }
            }
        }

        /*
         * 시작일이 가까운 과정 중 최대 5개만 선택하고,
         * 각 과정의 상세 API(L02)를 호출해
         * 일반훈련생 기준 본인부담액을 추가
         */
        return courses.stream()
                .sorted(
                        Comparator.comparingInt(
                                        (Work24ApiClient.TrainingCourse course) ->
                                                this.calculateTrainingScore(
                                                        course,
                                                        category
                                                )
                                )
                                .reversed()
                                .thenComparing(
                                        Work24ApiClient.TrainingCourse::getStartDate,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                )
                .limit(5)
                .map(course -> {

                    Long selfPayment =
                            this.work24ApiClient.findSelfPayment(
                                    course.getExternalCode(),
                                    course.getTrainingRound(),
                                    course.getInstitutionId()
                            );

                    return JobTrainingDTO.builder()
                            .externalCode(course.getExternalCode())
                            .trainingRound(course.getTrainingRound())
                            .institutionId(course.getInstitutionId())
                            .trainingName(course.getTrainingName())
                            .institutionName(course.getInstitutionName())
                            .trainingCost(course.getTrainingCost())
                            .selfPayment(selfPayment)
                            .address(course.getAddress())
                            .startDate(course.getStartDate())
                            .endDate(course.getEndDate())
                            .detailUrl(course.getDetailUrl())
                            .trainingType(course.getTrainingType())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private int calculateTrainingScore(
            final Work24ApiClient.TrainingCourse course,
            final JobCategoryVO category) {

        int score = 0;

        final String targetNcsCode =
                category.getNcsCode();

        final String courseNcsCode =
                course.getNcsCode();

        /*
         * 목표 직무의 NCS 코드와
         * 훈련과정의 NCS 코드가 가까울수록 높은 점수를 부여한다.
         */
        if (targetNcsCode != null
                && courseNcsCode != null) {

            if (targetNcsCode.equals(courseNcsCode)) {
                score += 100;

            } else if (targetNcsCode.length() >= 6
                    && courseNcsCode.startsWith(
                    targetNcsCode.substring(0, 6)
            )) {

                score += 60;

            } else if (targetNcsCode.length() >= 4
                    && courseNcsCode.startsWith(
                    targetNcsCode.substring(0, 4)
            )) {

                score += 30;
            }
        }

        /*
         * 직무명의 키워드가 훈련과정명에 포함되면
         * 추가 점수를 부여한다.
         */
        final String categoryName =
                category.getCategoryName();

        final String trainingName =
                course.getTrainingName();

        if (categoryName != null
                && trainingName != null) {

            final String[] keywords =
                    categoryName.split("·");

            for (final String keyword : keywords) {

                final String normalizedKeyword =
                        keyword.trim();

                if (!normalizedKeyword.isBlank()
                        && trainingName.contains(normalizedKeyword)) {

                    score += 10;
                }
            }
        }

        return score;
    }

    @Transactional
    @Override
    public void sendExamNotifications() {

        final LocalDate today =
                LocalDate.now();

        final List<JobExamNotificationDTO> targets =
                this.jobMapper.findExamNotificationTargetList();

        int sentCount = 0;

        for (final JobExamNotificationDTO target : targets) {

            sentCount += this.sendExamNotificationIfNeeded(
                    today,
                    target.getUserId(),
                    target.getQualName(),
                    "필기",
                    target.getWrittenExamDate()
            );

            sentCount += this.sendExamNotificationIfNeeded(
                    today,
                    target.getUserId(),
                    target.getQualName(),
                    "실기",
                    target.getPracticalExamDate()
            );
        }

        log.info(
                "자격증 시험 알림 배치 완료 - 대상 {}건, 발송 {}건",
                targets.size(),
                sentCount
        );
    }

    private int sendExamNotificationIfNeeded(
            final LocalDate today,
            final Long userId,
            final String qualName,
            final String examType,
            final LocalDate examDate
    ) {

        if (examDate == null) {
            return 0;
        }

        final long daysUntilExam =
                ChronoUnit.DAYS.between(
                        today,
                        examDate
                );

        if (daysUntilExam == EXAM_DDAY_7) {

            this.pushNotificationService.send(
                    userId,
                    qualName + " " + examType + "시험 D-7",
                    qualName + " " + examType + "시험이 7일 남았어요!",
                    PUSH_CATEGORY_JOB
            );

            return 1;
        }

        if (daysUntilExam == EXAM_DDAY_1) {

            this.pushNotificationService.send(
                    userId,
                    qualName + " " + examType + "시험 D-1",
                    qualName + " " + examType + "시험이 내일이에요!",
                    PUSH_CATEGORY_JOB
            );

            return 1;
        }

        if (daysUntilExam == EXAM_DDAY) {

            this.pushNotificationService.send(
                    userId,
                    qualName + " " + examType + "시험일",
                    "오늘은 "
                            + qualName
                            + " "
                            + examType
                            + "시험일이에요!",
                    PUSH_CATEGORY_JOB
            );

            return 1;
        }

        return 0;
    }
}
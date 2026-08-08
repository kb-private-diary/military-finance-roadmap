package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.JobProductDTO;
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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobServiceImpl implements JobService {

    private static final String GOAL_TYPE_EMPLOYMENT = "J01";
    private static final String GOAL_TYPE_PUBLIC_SERVICE = "J02";
    private static final String GOAL_TYPE_TRANSFER = "J03";

    private static final int ROADMAP_CATEGORY_JOB = 2;

    private final JobMapper jobMapper;
    private final ProductService productService;


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
    public JobGoalCreateResponseDTO createJobGoal(JobGoalCreateRequestDTO requestDTO) {

        JobGoalVO jobGoalVO = new JobGoalVO();
        jobGoalVO.setUserId(requestDTO.getUserId());
        jobGoalVO.setGoalType(requestDTO.getGoalType());
        jobGoalVO.setCategoryId(requestDTO.getCategoryId());
        jobGoalVO.setUnivId(requestDTO.getUnivId());
        jobGoalVO.setMajorId(requestDTO.getMajorId());
        jobGoalVO.setExpectedDate(requestDTO.getExpectedDate());
        jobGoalVO.setCreatedNm(String.valueOf(requestDTO.getUserId()));

        this.jobMapper.insertJobGoal(jobGoalVO);

        return new JobGoalCreateResponseDTO(jobGoalVO.getGoalId());
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
            courseVOList = this.jobMapper.findCourseListByQualificationIds(qualIds);

        } else if (GOAL_TYPE_PUBLIC_SERVICE.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByCategoryId(jobGoalVO.getCategoryId());

            courseVOList = this.jobMapper.findCourseListByCategoryId(jobGoalVO.getCategoryId());

        } else if (GOAL_TYPE_TRANSFER.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByMajorId(jobGoalVO.getMajorId());

            courseVOList = this.jobMapper.findCourseListByMajorId(jobGoalVO.getMajorId());

        } else {
            throw BusinessException.badRequest("올바르지 않은 목표유형입니다", "JOB_008");
        }

        List<JobQualificationDTO> qualifications =qualificationVOList.stream()
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
    public JobPlanCreateResponseDTO createJobPlans(Long goalId, JobPlanCreateRequestDTO requestDTO) {
        JobGoalVO jobGoalVO = this.findJobGoalOrThrow(goalId);

        List<Long> qualIds = requestDTO.getQualIds();
        List<Long> courseIds = requestDTO.getCourseIds();

        boolean hasQualification = qualIds != null && !qualIds.isEmpty();
        boolean hasCourse = courseIds != null && !courseIds.isEmpty();

        if (!hasQualification && !hasCourse) {
            throw BusinessException.badRequest("준비항목을 1개 이상 선택해주세요", "JOB_003");
        }

        List<JobQualificationVO> qualificationVOList = hasQualification
                ? this.jobMapper.findQualificationListByIds(qualIds)
                : List.of();

        List<JobCourseVO> courseVOList = hasCourse
                ? this.jobMapper.findCourseListByIds(courseIds)
                : List.of();

        if (hasQualification && qualificationVOList.size() != qualIds.size()) {
            throw BusinessException.notFound("존재하지 않는 자격증·어학이 포함되어 있습니다", "JOB_004");
        }

        if (hasCourse && courseVOList.size() != courseIds.size()) {
            throw BusinessException.notFound("존재하지 않는 인강이 포함되어 있습니다", "JOB_004");
        }

        String userName = String.valueOf(jobGoalVO.getUserId());

        this.jobMapper.deleteGoalQualificationByGoalId(goalId, userName);
        this.jobMapper.deleteGoalCourseByGoalId(goalId, userName);

        if (hasQualification) {
            this.jobMapper.insertGoalQualificationList(goalId, qualIds, userName);
        }

        if (hasCourse) {
            this.jobMapper.insertGoalCourseList(goalId, courseIds, userName);
        }

        return JobPlanCreateResponseDTO.builder()
                .goalId(goalId)
                .qualifications(qualificationVOList.stream().map(JobQualificationDTO::of).toList())
                .courses(courseVOList.stream().map(JobCourseDTO::of).toList())
                .build();
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

        // 목표에 저장된 자격증·어학 조회
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

        // 목표유형에 맞는 정책·KB 서비스 추천 조회
        ServiceRecommendResponseDTO services =
                this.findServiceRecommend(goalId);

        detail.setQualifications(qualifications);
        detail.setCourses(courses);
        detail.setPolicies(services.getPolicies());
        detail.setFinancialProducts(services.getFinancialProducts());

        return detail;
    }

    // 진로 목표 저장 확정
    @Override
    @Transactional
    public void confirmJobGoal(Long goalId) {
        JobGoalVO jobGoalVO = this.findJobGoalOrThrow(goalId);

        String modifiedNm = String.valueOf(jobGoalVO.getUserId());

        this.jobMapper.updateJobGoalStatus(
                goalId,
                "CONFIRMED",
                modifiedNm
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

    private long calculateQualificationCost(JobQualificationVO qualificationVO) {
        if (qualificationVO.getMilitaryFee() != null) {
            return qualificationVO.getMilitaryFee();
        }

        long writtenFee = qualificationVO.getWrittenFee() == null ? 0L : qualificationVO.getWrittenFee();
        long practicalFee = qualificationVO.getPracticalFee() == null ? 0L : qualificationVO.getPracticalFee();

        return writtenFee + practicalFee;
    }

    private long calculateCourseCost(JobCourseVO courseVO) {
        if (courseVO.getMilitaryPrice() != null) {
            return courseVO.getMilitaryPrice();
        }

        if (courseVO.getDiscountPrice() != null) {
            return courseVO.getDiscountPrice();
        }

        return courseVO.getOriginalPrice() == null ? 0L : courseVO.getOriginalPrice();
    }
}
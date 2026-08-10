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
            courseVOList = this.jobMapper.findCourseListByQualificationIds(qualIds);

        } else if (GOAL_TYPE_PUBLIC_SERVICE.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByCategoryId(jobGoalVO.getCategoryId());

            courseVOList = this.jobMapper.findCourseListByCategoryId(jobGoalVO.getCategoryId());

        } else if (GOAL_TYPE_TRANSFER.equals(jobGoalVO.getGoalType())) {
            qualificationVOList = this.jobMapper.findQualificationListByMajorId(jobGoalVO.getMajorId());

            courseVOList = this.jobMapper.findCourseListByMajorId(jobGoalVO.getMajorId());

        } else {
            throw BusinessException.badRequest("올바르지 않은 목표유형입니다", "JOB_004");
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
    public JobPlanCreateResponseDTO createJobPlans(
            Long goalId,
            Long userId,
            String username,
            JobPlanCreateRequestDTO requestDTO) {
        JobGoalVO jobGoalVO = this.findOwnedJobGoalOrThrow(userId, goalId);

        List<Long> qualIds = requestDTO.getQualIds();
        List<Long> courseIds = requestDTO.getCourseIds();

        boolean hasQualification = qualIds != null && !qualIds.isEmpty();
        boolean hasCourse = courseIds != null && !courseIds.isEmpty();

        // 자격증·어학 또는 인강을 하나 이상 선택해야 함
        if (!hasQualification && !hasCourse) {
            throw BusinessException.badRequest("준비항목을 1개 이상 선택해주세요", "JOB_002");
        }

        List<JobQualificationVO> qualificationVOList = hasQualification
                ? this.jobMapper.findQualificationListByIds(qualIds)
                : List.of();

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

        // 기존 선택 항목 soft delete 후 새 선택 항목 저장
        this.jobMapper.deleteGoalQualificationByGoalId(goalId, username);
        this.jobMapper.deleteGoalCourseByGoalId(goalId, username);

        if (hasQualification) {
            this.jobMapper.insertGoalQualificationList(goalId, qualIds, username);
        }

        if (hasCourse) {
            this.jobMapper.insertGoalCourseList(goalId, courseIds, username);
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


    // 진로 목표 삭제
    // 목표 존재 여부를 확인한 후 del_yn을 'Y'로 변경하여 soft delete 처리
    @Override
    @Transactional
    public void deleteJobGoal(
            Long goalId,
            Long userId,
            String username) {

        // 목표 존재 여부 + 로그인 회원의 목표인지 확인
        this.findOwnedJobGoalOrThrow(userId, goalId);

        // 진로 목표 soft delete
        this.jobMapper.deleteJobGoalById(goalId, username);
    }
}
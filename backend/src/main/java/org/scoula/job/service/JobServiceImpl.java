package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobInterestedTypeVO;
import org.scoula.job.domain.JobPlanVO;
import org.scoula.job.domain.PrepItemCriteriaVO;
import org.scoula.job.domain.ServiceCriteriaVO;
import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.JobPlanItemDTO;
import org.scoula.job.dto.JobProductDTO;
import org.scoula.job.dto.PrepItemDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
import org.scoula.job.dto.ServiceRecommendResponseDTO;
import org.scoula.job.mapper.JobMapper;
import org.scoula.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobServiceImpl implements JobService {

    /** 로드맵 카테고리 코드 (roadmap_category) — 여행1 진로2 */
    private static final int ROADMAP_CATEGORY_JOB = 2;

    /** 서비스 구분 코드 — G01 정부 정책 / G02 KB 금융서비스 */
    private static final String SERVICE_TYPE_POLICY = "G01";

    private final JobMapper jobMapper;
    private final ProductService productService;


    @Override
    @Transactional(readOnly = true)
    public List<JobCodeDTO> findJobCodes(String goalType) {
        return this.jobMapper.findJobCodeListByGoalType(goalType).stream()
                .map(JobCodeDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobGoalCreateResponseDTO createJobGoal(JobGoalCreateRequestDTO requestDTO) {
        // 준비 항목 유형은 필수값이므로 목표 저장 전에 먼저 검증
        List<String> itemTypes = requestDTO.getItemTypes();
        if (itemTypes == null || itemTypes.isEmpty()) {
            throw BusinessException.badRequest("준비항목 유형을 1개 이상 선택해주세요", "JOB_005");
        }

        JobGoalVO jobGoalVO = new JobGoalVO();
        jobGoalVO.setUserId(requestDTO.getUserId());
        jobGoalVO.setGoalType(requestDTO.getGoalType());
        jobGoalVO.setJobCodeId(requestDTO.getJobCodeId());
        jobGoalVO.setExpectedDate(requestDTO.getExpectedDate());

        this.jobMapper.insertJobGoal(jobGoalVO);

        // 선택된 준비 항목(P01/P02/P03)을 goal_id 기준으로 각 한 줄씩 저장
        // job_goal insert와 같은 @Transactional 범위 안이라, 중간에 실패하면 전체 롤백됨
        for (String itemType : itemTypes) {
            JobInterestedTypeVO jobInterestedTypeVO = new JobInterestedTypeVO();
            jobInterestedTypeVO.setGoalId(jobGoalVO.getGoalId());
            jobInterestedTypeVO.setItemType(itemType);
            // TODO: JWT 미연동으로 인해 job_goal insert와 동일하게 임시로 userId를 문자열로 기록
            jobInterestedTypeVO.setCreatedNm(String.valueOf(requestDTO.getUserId()));
            this.jobMapper.insertJobInterestedType(jobInterestedTypeVO);
        }

        return new JobGoalCreateResponseDTO(jobGoalVO.getGoalId());
    }

    @Override
    @Transactional(readOnly = true)
    public PrepItemRecommendResponseDTO findPrepItemRecommend(Long goalId) {
        JobGoalVO jobGoalVO = this.jobMapper.findJobGoal(goalId);
        if (jobGoalVO == null) {
            throw BusinessException.notFound("진로 목표를 찾을 수 없습니다", "JOB_001");
        }

        List<String> itemTypes = this.jobMapper.findInterestedItemTypeList(goalId);

        // 목표 등록 시 준비 항목 유형을 필수로 받으므로 정상 흐름에서는 비어 있을 수 없음
        if (itemTypes.isEmpty()) {
            throw BusinessException.notFound("준비항목 유형이 선택되지 않았습니다", "JOB_002");
        }

        List<PrepItemCriteriaVO> prepItemCriteriaVOList = this.jobMapper.findPrepItemCriteriaList(
                jobGoalVO.getGoalType(),
                jobGoalVO.getJobCodeId(),
                itemTypes
        );

        // item_type(P01/P02/P03) 별로 그룹핑
        LinkedHashMap<String, List<PrepItemDTO>> groupedItems = prepItemCriteriaVOList.stream()
                .map(PrepItemDTO::of)
                .collect(Collectors.groupingBy(
                        PrepItemDTO::getItemType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return new PrepItemRecommendResponseDTO(goalId, groupedItems);
    }

    @Override
    @Transactional
    public JobPlanCreateResponseDTO createJobPlans(Long goalId, JobPlanCreateRequestDTO requestDTO) {

        JobGoalVO jobGoalVO = this.jobMapper.findJobGoal(goalId);
        if (jobGoalVO == null) {
            throw BusinessException.notFound("진로 목표를 찾을 수 없습니다", "JOB_001");
        }

        List<Long> prepCritIds = requestDTO.getPrepCritIds();
        if (prepCritIds == null || prepCritIds.isEmpty()) {
            throw BusinessException.badRequest("준비항목을 1개 이상 선택해주세요", "JOB_003");
        }

        List<PrepItemCriteriaVO> criteriaList = this.jobMapper.findPrepItemCriteriaListByIds(prepCritIds);
        if (criteriaList.size() != prepCritIds.size()) {
            throw BusinessException.notFound("존재하지 않는 준비항목이 포함되어 있습니다", "JOB_004");
        }

        List<JobPlanVO> jobPlans = criteriaList.stream()
                .map(criteria -> {
                    JobPlanVO plan = new JobPlanVO();
                    plan.setGoalId(goalId);
                    plan.setItemType(criteria.getItemType());
                    plan.setItemName(criteria.getItemName());
                    plan.setInfoUrl(criteria.getInfoUrl());
                    plan.setApplyUrl(criteria.getApplyUrl());
                    plan.setAmount(criteria.getAmount());
                    return plan;
                })
                .collect(Collectors.toList());

        // TODO: JWT 미연동으로 인해 임시로 userId를 문자열로 기록
        String userName = String.valueOf(jobGoalVO.getUserId());

        // 스냅샷 방식이므로 재선택 시 기존 항목을 소프트 삭제하고 다시 저장
        this.jobMapper.deleteJobPlanListByGoalId(goalId, userName);
        this.jobMapper.insertJobPlanList(jobPlans, userName);

        // amount는 nullable이므로 값이 없는 항목은 0으로 계산
        long totalAmount = jobPlans.stream()
                .mapToLong(plan -> plan.getAmount() == null ? 0L : plan.getAmount())
                .sum();

        List<JobPlanItemDTO> items = jobPlans.stream()
                .map(JobPlanItemDTO::of)
                .collect(Collectors.toList());

        return JobPlanCreateResponseDTO.builder()
                .goalId(goalId)
                .totalAmount(totalAmount)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRecommendResponseDTO findServiceRecommend(Long goalId) {
        JobGoalVO jobGoalVO = this.findJobGoalOrThrow(goalId);

        List<ServiceCriteriaVO> criteriaList =
                this.jobMapper.findServiceCriteriaListByGoalType(jobGoalVO.getGoalType());

        return ServiceRecommendResponseDTO.builder()
                .goalId(goalId)
                .policies(this.toPolicies(criteriaList))
                .financialProducts(this.toFinancialProducts(criteriaList))
                .build();
    }

    // 진로 목표를 조회하고 존재하지 않으면 예외를 던진다
    private JobGoalVO findJobGoalOrThrow(Long goalId) {
        JobGoalVO jobGoalVO = this.jobMapper.findJobGoal(goalId);
        if (jobGoalVO == null) {
            throw BusinessException.notFound("진로 목표를 찾을 수 없습니다", "JOB_001");
        }
        return jobGoalVO;
    }

    // 정부 정책(G01)만 골라 정책 목록으로 변환한다
    private List<JobProductDTO> toPolicies(List<ServiceCriteriaVO> criteriaList) {
        return criteriaList.stream()
                .filter(criteria -> SERVICE_TYPE_POLICY.equals(criteria.getServiceType()))
                .map(JobProductDTO::ofService)
                .toList();
    }

    // KB 금융서비스(G02)와 카드 상품을 하나의 금융상품 목록으로 합친다
    private List<JobProductDTO> toFinancialProducts(List<ServiceCriteriaVO> criteriaList) {
        List<JobProductDTO> financialProducts = criteriaList.stream()
                .filter(criteria -> !SERVICE_TYPE_POLICY.equals(criteria.getServiceType()))
                .map(JobProductDTO::ofService)
                .collect(Collectors.toCollection(ArrayList::new));

        this.productService.findCardProductListByCategory(ROADMAP_CATEGORY_JOB).stream()
                .map(JobProductDTO::ofCard)
                .forEach(financialProducts::add);

        return financialProducts;
    }
}
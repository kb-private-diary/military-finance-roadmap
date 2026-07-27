package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobInterestedTypeVO;
import org.scoula.job.domain.JobPlanVO;
import org.scoula.job.domain.PrepItemCriteriaVO;
import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.PrepItemDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
import org.scoula.job.mapper.JobMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobServiceImpl implements JobService{

    private final JobMapper jobMapper;

    @Override
    public List<JobCodeDTO> findJobCodes(String goalType) {
        return jobMapper.findJobCodesByGoalType(goalType).stream()
                .map(JobCodeDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobGoalCreateResponseDTO createJobGoal(JobGoalCreateRequestDTO requestDTO) {
        JobGoalVO jobGoalVO = new JobGoalVO();
        jobGoalVO.setUserId(requestDTO.getUserId());
        jobGoalVO.setGoalType(requestDTO.getGoalType());
        jobGoalVO.setJobCodeId(requestDTO.getJobCodeId());
        jobGoalVO.setExpectedDate(requestDTO.getExpectedDate());

        jobMapper.createJobGoal(jobGoalVO);

        // 선택된 준비 항목(P01/P02/P03)을 goal_id 기준으로 각 한 줄씩 저장
        // job_goal insert와 같은 @Transactional 범위 안이라, 중간에 실패하면 전체 롤백됨
        List<String> itemTypes = requestDTO.getItemTypes();
        if (itemTypes != null) {
            for (String itemType : itemTypes) {
                JobInterestedTypeVO jobInterestedTypeVO = new JobInterestedTypeVO();
                jobInterestedTypeVO.setGoalId(jobGoalVO.getGoalId());
                jobInterestedTypeVO.setItemType(itemType);
                // TODO: JWT 미연동으로 인해 job_goal insert와 동일하게 임시로 userId를 문자열로 기록
                jobInterestedTypeVO.setCreatedNm(String.valueOf(requestDTO.getUserId()));
                jobMapper.createJobInterestedType(jobInterestedTypeVO);
            }
        }

        return new JobGoalCreateResponseDTO(jobGoalVO.getGoalId());
    }

    @Override
    public PrepItemRecommendResponseDTO findPrepItemRecommend(Long goalId) {
        JobGoalVO jobGoalVO = jobMapper.findJobGoal(goalId);
        if (jobGoalVO == null) {
            // 에러코드 등록: JOB_001 / 404 / "진로 목표를 찾을 수 없습니다" / JOB / 지원
            throw BusinessException.notFound("진로 목표를 찾을 수 없습니다", "JOB_001");
        }

        List<String> itemTypes = jobMapper.findInterestedItemTypes(goalId);

        // 선택된 준비 항목이 없으면 조회 자체를 생략하고 빈 결과 반환 (item_type IN () SQL 오류 방지)
        if (itemTypes.isEmpty()) {
            return new PrepItemRecommendResponseDTO(goalId, Collections.emptyMap());
        }

        List<PrepItemCriteriaVO> prepItemCriteriaVOList = jobMapper.findPrepItemCriteriaList(
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
    public void createJobPlans(Long goalId, JobPlanCreateRequestDTO requestDTO) {

        JobGoalVO jobGoal = jobMapper.findJobGoal(goalId);
        if (jobGoal == null) {
            throw BusinessException.notFound("진로 목표를 찾을 수 없습니다", "JOB_001");
        }

        List<Long> prepCritIds = requestDTO.getPrepCritIds();
        if (prepCritIds == null || prepCritIds.isEmpty()) {
            throw BusinessException.badRequest("준비항목을 1개 이상 선택해주세요", "JOB_003");
        }

        List<PrepItemCriteriaVO> criteriaList = jobMapper.findPrepItemCriteriaByIds(prepCritIds);
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

        jobMapper.createJobPlans(jobPlans, String.valueOf(jobGoal.getUserId()));
    }
}

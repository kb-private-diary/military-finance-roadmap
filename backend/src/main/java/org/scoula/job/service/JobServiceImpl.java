package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.job.domain.JobCodeVO;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobInterestedTypeVO;
import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.mapper.JobMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

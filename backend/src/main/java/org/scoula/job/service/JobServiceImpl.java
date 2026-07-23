package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.job.domain.JobCodeVO;
import org.scoula.job.domain.JobGoalVO;
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

        return new JobGoalCreateResponseDTO(jobGoalVO.getGoalId());
    }
}

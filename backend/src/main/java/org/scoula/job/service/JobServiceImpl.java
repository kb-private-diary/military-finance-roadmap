package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.job.domain.JobCodeVO;
import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.mapper.JobMapper;
import org.springframework.stereotype.Service;

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
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private JobCodeDTO toDTO(JobCodeVO vo) {
        return JobCodeDTO.builder()
                .jobCodeId(vo.getJobCodeId())
                .goalType(vo.getGoalType())
                .codeName(vo.getCodeName())
                .infoUrl(vo.getInfoUrl())
                .build();
    }
}

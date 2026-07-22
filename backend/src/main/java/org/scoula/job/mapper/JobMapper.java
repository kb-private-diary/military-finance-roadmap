package org.scoula.job.mapper;

import org.scoula.job.domain.JobCodeVO;

import java.util.List;

public interface JobMapper {
    // goalType(J01/J02/J03)에 해당하는 직무·직렬·학과 코드 목록 조회
    List<JobCodeVO> findJobCodesByGoalType(String goalType);
}

package org.scoula.job.mapper;

import org.scoula.job.domain.JobCodeVO;
import org.scoula.job.domain.JobGoalVO;

import java.util.List;

public interface JobMapper {
    // goalType(J01/J02/J03)에 해당하는 직무·직렬·학과 코드 목록 조회
    List<JobCodeVO> findJobCodesByGoalType(String goalType);

    // 진로 목표 신규 등록
    void createJobGoal(JobGoalVO jobGoalVO);
}

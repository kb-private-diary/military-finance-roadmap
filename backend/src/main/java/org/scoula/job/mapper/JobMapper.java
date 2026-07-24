package org.scoula.job.mapper;

import org.scoula.job.domain.JobCodeVO;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobInterestedTypeVO;

import java.util.List;

public interface JobMapper {
    // goalType(J01/J02/J03)에 해당하는 직무·직렬·학과 코드 목록 조회
    List<JobCodeVO> findJobCodesByGoalType(String goalType);

    // 진로 목표 신규 등록
    void createJobGoal(JobGoalVO jobGoalVO);

    // 진로 목표에 선택된 준비 항목 등록 (goal_id, item_type 조합 UNIQUE)
    void createJobInterestedType(JobInterestedTypeVO jobInterestedTypeVO);
}

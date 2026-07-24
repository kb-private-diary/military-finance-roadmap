package org.scoula.job.service;

import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;

import java.util.List;

public interface JobService {
    // goalType에 해당하는 직무·직렬·학과 코드 목록 조회
    List<JobCodeDTO> findJobCodes(String goalType);

    // 진로 목표 신규 등록, 생성된 goalId를 담은 응답 DTO 반환
    JobGoalCreateResponseDTO createJobGoal(JobGoalCreateRequestDTO requestDTO);

    // goalId 기준 준비 항목 추천 조회 (item_type별 그룹핑)
    PrepItemRecommendResponseDTO findPrepItemRecommend(Long goalId);
}

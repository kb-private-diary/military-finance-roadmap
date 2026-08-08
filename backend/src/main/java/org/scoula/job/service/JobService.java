package org.scoula.job.service;

import org.scoula.job.dto.JobCategoryDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.JobTransferMajorDTO;
import org.scoula.job.dto.JobTransferUniversityDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
import org.scoula.job.dto.ServiceRecommendResponseDTO;

import java.util.List;

public interface JobService {
    // 취업·공무원 분류 목록 조회
    List<JobCategoryDTO> findCategoryList(String goalType);

    // 편입 대학 목록 조회
    List<JobTransferUniversityDTO> findTransferUniversityList();

    // 선택 대학의 편입 모집 학과계열 목록 조회
    List<JobTransferMajorDTO> findTransferMajorList(Long univId);

    // 진로 목표 신규 등록
    JobGoalCreateResponseDTO createJobGoal(JobGoalCreateRequestDTO requestDTO);

    // 목표 기준 자격증·어학·인강 추천 조회
    PrepItemRecommendResponseDTO findPrepItemRecommend(Long goalId);

    // 선택한 자격증·인강 저장
    JobPlanCreateResponseDTO createJobPlans(
            Long goalId,
            JobPlanCreateRequestDTO requestDTO
    );

    // 목표 기준 정책·KB 서비스 추천 조회
    ServiceRecommendResponseDTO findServiceRecommend(Long goalId);

    // 진로 목표 상세 조회
    JobGoalDetailResponseDTO findJobGoalDetail(Long goalId);

    // 진로 로드맵 저장 확정
    void confirmJobGoal(Long goalId);
}

package org.scoula.job.service;

import org.scoula.job.dto.JobCategoryDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.JobTrainingDTO;
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
    JobGoalCreateResponseDTO createJobGoal(
            Long userId,
            String username,
            JobGoalCreateRequestDTO requestDTO
    );

    // 작성 중인 진로 목표 수정
    void updateJobGoal(
            Long goalId,
            Long userId,
            String username,
            JobGoalCreateRequestDTO requestDTO
    );

    // 목표 기준 자격증·어학·인강 추천 조회
    PrepItemRecommendResponseDTO findPrepItemRecommend(Long goalId);

    // 선택한 자격증·인강·훈련과정 저장
    JobPlanCreateResponseDTO createJobPlans(
            Long goalId,
            Long userId,
            String username,
            JobPlanCreateRequestDTO requestDTO
    );

    // 목표 기준 정책·KB 서비스 추천 조회
    ServiceRecommendResponseDTO findServiceRecommend(Long goalId);

    // 진로 목표 상세 조회 (최근 3개월 월평균 후회소비 포함)
    JobGoalDetailResponseDTO findJobGoalDetail(Long goalId, Long userId);

    // 진로 로드맵 저장 확정
    void confirmJobGoal(
            Long goalId,
            Long userId,
            String username
    );

    // 작성 중인 진로 목표 조회
    JobGoalDetailResponseDTO findCurrentJobGoal(Long userId);

    // 선택한 직무와 지역을 기준으로 고용24 훈련과정 추천 조회
    List<JobTrainingDTO> findTrainingRecommend(
            Long goalId,
            String regionCode
    );

    // 사용자가 선택한 자격증 시험일 웹푸시 알림 발송
    void sendExamNotifications();
}

package org.scoula.job.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.job.dto.JobCategoryDTO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;
import org.scoula.job.dto.JobTrainingDTO;
import org.scoula.job.dto.JobTransferMajorDTO;
import org.scoula.job.dto.JobTransferUniversityDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
import org.scoula.job.dto.ServiceRecommendResponseDTO;
import org.scoula.job.service.JobService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
@Log4j2
public class JobController {

    private final JobService jobService;

    // GET /api/job/categories?goalType=J01
    // 목표 유형별 직무·직렬·학과 카테고리 조회
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<JobCategoryDTO>>> findCategoryList(
            @RequestParam String goalType) {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findCategoryList(goalType))
        );
    }

    // GET /api/job/transfer-universities
    // 편입 대상 대학교 목록 조회
    @GetMapping("/transfer-universities")
    public ResponseEntity<ApiResponse<List<JobTransferUniversityDTO>>> findTransferUniversityList() {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findTransferUniversityList())
        );
    }

    // GET /api/job/transfer-universities/{univId}/majors
    // 선택한 대학교의 편입 학과 목록 조회
    @GetMapping("/transfer-universities/{univId}/majors")
    public ResponseEntity<ApiResponse<List<JobTransferMajorDTO>>> findTransferMajorList(
            @PathVariable Long univId) {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findTransferMajorList(univId))
        );
    }

    // POST /api/job/goals  → 진로 목표 등록
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<JobGoalCreateResponseDTO>> createJobGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @Valid @RequestBody JobGoalCreateRequestDTO requestDTO) {
        JobGoalCreateResponseDTO responseDTO = this.jobService.createJobGoal(
                customUser.getMember().getId(),
                customUser.getUsername(),
                requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    // PATCH /api/job/goals/{goalId} → 작성 중인 진로 목표 수정
    @PatchMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<Void>> updateJobGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId,
            @Valid @RequestBody JobGoalCreateRequestDTO requestDTO) {

        this.jobService.updateJobGoal(
                goalId,
                customUser.getMember().getId(),
                customUser.getUsername(),
                requestDTO
        );

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 작성 중인 진로 목표 조회
    // 사용자의 DRAFT 상태 목표가 있으면 해당 목표 상세 정보를 반환
    // 작성 중인 목표가 없으면 null 반환
    @GetMapping("/goals/current")
    public ResponseEntity<ApiResponse<JobGoalDetailResponseDTO>> findCurrentJobGoal(
            @AuthenticationPrincipal CustomUser customUser) {

        return ResponseEntity.ok(ApiResponse.success(this.jobService.findCurrentJobGoal(
                customUser.getMember().getId()
        )));
    }

    // GET /api/job/goals/{goalId}/prep-items  → 준비항목 추천 조회
    @GetMapping("/goals/{goalId}/prep-items")
    public ResponseEntity<ApiResponse<PrepItemRecommendResponseDTO>> findPrepItemRecommend(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(this.jobService.findPrepItemRecommend(goalId)));
    }

    // GET /api/job/goals/{goalId}/trainings?regionCode=11
    // 선택한 직무와 지역을 기준으로 고용24 훈련과정 추천 조회
    @GetMapping("/goals/{goalId}/trainings")
    public ResponseEntity<ApiResponse<List<JobTrainingDTO>>> findTrainingRecommend(
            @PathVariable Long goalId,
            @RequestParam String regionCode) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        this.jobService.findTrainingRecommend(
                                goalId,
                                regionCode
                        )
                )
        );
    }

    // POST /api/job/goals/{goalId}/plans → 준비항목 선택 저장
    @PostMapping("/goals/{goalId}/plans")
    public ResponseEntity<ApiResponse<JobPlanCreateResponseDTO>> createJobPlans(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId,
            @RequestBody JobPlanCreateRequestDTO requestDTO) {
        JobPlanCreateResponseDTO responseDTO = this.jobService.createJobPlans(
                goalId,
                customUser.getMember().getId(),
                customUser.getUsername(),
                requestDTO
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    // GET /api/job/goals/{goalId}/services → 정책·서비스 추천 조회
    @GetMapping("/goals/{goalId}/services")
    public ResponseEntity<ApiResponse<ServiceRecommendResponseDTO>> findServiceRecommend(
            @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(this.jobService.findServiceRecommend(goalId)));
    }

    // GET /api/job/goals/{goalId} → 목표 상세 조회
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<JobGoalDetailResponseDTO>> findJobGoalDetail(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findJobGoalDetail(
                        goalId,
                        customUser.getMember().getId()
                ))
        );
    }

    // POST /api/job/goals/{goalId}/confirm → 진로 로드맵 저장 확정
    @PostMapping("/goals/{goalId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmJobGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {

        this.jobService.confirmJobGoal(
                goalId,
                customUser.getMember().getId(),
                customUser.getUsername()
        );

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

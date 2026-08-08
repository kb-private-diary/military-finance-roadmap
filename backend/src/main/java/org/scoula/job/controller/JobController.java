package org.scoula.job.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.job.dto.JobCategoryDTO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;
import org.scoula.job.dto.JobTransferMajorDTO;
import org.scoula.job.dto.JobTransferUniversityDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.JobPlanCreateResponseDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
import org.scoula.job.dto.ServiceRecommendResponseDTO;
import org.scoula.job.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<JobCategoryDTO>>> findCategoryList(
            @RequestParam String goalType) {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findCategoryList(goalType))
        );
    }

    @GetMapping("/transfer-universities")
    public ResponseEntity<ApiResponse<List<JobTransferUniversityDTO>>> findTransferUniversityList() {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findTransferUniversityList())
        );
    }

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
            // TODO: 실제 구현에서는 JWT 기반 보안(SecurityContext)에서 userId(=user.id)를 가져와야 합니다.
            // 개발 편의를 위해 임시로 요청 body에 포함해서 받도록 설정합니다.
            @Valid @RequestBody JobGoalCreateRequestDTO requestDTO) {
        JobGoalCreateResponseDTO responseDTO = jobService.createJobGoal(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    // GET /api/job/goals/{goalId}/prep-items  → 준비항목 추천 조회
    @GetMapping("/goals/{goalId}/prep-items")
    public ResponseEntity<ApiResponse<PrepItemRecommendResponseDTO>> findPrepItemRecommend(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(jobService.findPrepItemRecommend(goalId)));
    }

    // GET /api/job/goals/{goalId}/plans → 준비항목 선택 저장
    @PostMapping("/goals/{goalId}/plans")
    public ResponseEntity<ApiResponse<JobPlanCreateResponseDTO>> createJobPlans(
            @PathVariable Long goalId,
            @RequestBody JobPlanCreateRequestDTO requestDTO) {
        JobPlanCreateResponseDTO responseDTO = this.jobService.createJobPlans(goalId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    // GET /api/job/goals/{goalId}/services → 정책·서비스 추천 조회
    @GetMapping("/goals/{goalId}/services")
    public ResponseEntity<ApiResponse<ServiceRecommendResponseDTO>> findServiceRecommend(
            @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(this.jobService.findServiceRecommend(goalId)));
    }

    // GET /api/job/goals/{goalId} → 목표 상세
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<JobGoalDetailResponseDTO>> findJobGoalDetail(
            @PathVariable Long goalId) {
        return ResponseEntity.ok(
                ApiResponse.success(this.jobService.findJobGoalDetail(goalId))
        );
    }

    // POST /api/job/goals/{goalId}/confirm → 진로 로드맵 저장 확정
    @PostMapping("/goals/{goalId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmJobGoal(
            @PathVariable Long goalId) {

        this.jobService.confirmJobGoal(goalId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

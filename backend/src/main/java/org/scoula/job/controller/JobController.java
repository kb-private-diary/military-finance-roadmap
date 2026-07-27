package org.scoula.job.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.dto.JobGoalCreateRequestDTO;
import org.scoula.job.dto.JobGoalCreateResponseDTO;
import org.scoula.job.dto.JobPlanCreateRequestDTO;
import org.scoula.job.dto.PrepItemRecommendResponseDTO;
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

import java.util.List;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
@Log4j2
public class JobController {

    private final JobService jobService;

    //JOB-API-01: 직무·직렬·학과 코드 조회(goalType별)
    @GetMapping("/codes")
    public ResponseEntity<ApiResponse<List<JobCodeDTO>>> findJobCodes(@RequestParam String goalType) {
        log.info("Fetching job codes for goalType: {}", goalType);
        return ResponseEntity.ok(ApiResponse.success(jobService.findJobCodes(goalType)));
    }

    //JOB-API-02: 진로 목표 신규 등록
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<JobGoalCreateResponseDTO>> createJobGoal(
            // TODO: 실제 구현에서는 JWT 기반 보안(SecurityContext)에서 userId(=user.id)를 가져와야 합니다.
            // 개발 편의를 위해 임시로 요청 body에 포함해서 받도록 설정합니다.
            @RequestBody JobGoalCreateRequestDTO requestDTO) {
        log.info("Creating job goal for userId: {}", requestDTO.getUserId());
        JobGoalCreateResponseDTO responseDTO = jobService.createJobGoal(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    //JOB-API-04: 준비항목 추천 조회
    @GetMapping("/goals/{goalId}/prep-items")
    public ResponseEntity<ApiResponse<PrepItemRecommendResponseDTO>> findPrepItemRecommend(@PathVariable Long goalId) {
        log.info("Fetching job recommend for goalId: {}", goalId);
        return ResponseEntity.ok(ApiResponse.success(jobService.findPrepItemRecommend(goalId)));
    }

    //JOB-API-05: 준비항목 선택 저장
    @PostMapping("/goals/{goalId}/plans")
    public ResponseEntity<ApiResponse<Void>> createJobPlans(
            @PathVariable Long goalId,
            @RequestBody JobPlanCreateRequestDTO requestDTO) {
        log.info("Creating job plans for goalId: {}", goalId);
        jobService.createJobPlans(goalId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }
}

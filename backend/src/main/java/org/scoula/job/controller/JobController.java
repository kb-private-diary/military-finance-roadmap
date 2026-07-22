package org.scoula.job.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.job.dto.JobCodeDTO;
import org.scoula.job.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

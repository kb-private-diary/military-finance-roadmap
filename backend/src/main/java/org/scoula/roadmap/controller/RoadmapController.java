package org.scoula.roadmap.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.roadmap.dto.RoadmapListResponseDTO;
import org.scoula.roadmap.service.RoadmapService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
@Log4j2
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping("/goals/{category}")
    public ResponseEntity<ApiResponse<List<RoadmapListResponseDTO>>> getRoadmapGoals(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable String category) {

        Long userId = customUser.getMember().getId();

        log.info("Fetching roadmap goals. userId={}, category={}", userId, category);

        List<RoadmapListResponseDTO> responseDTO =
                this.roadmapService.findRoadmapList(userId, category);

        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoadmapGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId,
            @RequestParam Long categoryId) {

        Long userId = customUser.getMember().getId();
        String username = customUser.getUsername();

        log.info(
                "Deleting roadmap goal. userId={}, categoryId={}, goalId={}",
                userId,
                categoryId,
                goalId
        );

        this.roadmapService.deleteRoadmapGoal(
                userId,
                categoryId,
                goalId,
                username
        );

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

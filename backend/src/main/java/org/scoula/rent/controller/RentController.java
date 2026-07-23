package org.scoula.rent.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.response.ApiResponse;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.service.RentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
public class RentController {

    private final RentService service;

    // GET /api/rent/regions            → 시도 목록
    // GET /api/rent/regions?sido=서울   → 시군구 목록
    // GET /api/rent/regions?sido=서울&sigunguCode=11680 → 읍면동 목록
    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionResponseDTO>>> findRegions(
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigunguCode) {

        List<RegionResponseDTO> result = service.findRegions(sido, sigunguCode);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
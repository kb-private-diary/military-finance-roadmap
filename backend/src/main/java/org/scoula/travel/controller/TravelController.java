package org.scoula.travel.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.response.ApiResponse;
import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.service.TravelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 여행 로드맵 REST 컨트롤러
*/
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travel")
@Api(tags = "여행 로드맵")
public class TravelController {

    private final TravelService service;

    @GetMapping("/cities")
    @ApiOperation(value = "도시 물가 목록 조회",
            notes = "나라별 스타일(saving/common/premium)별 1인 1일 물가를 조회한다.")
    public ResponseEntity<ApiResponse<List<CityCostResponseDTO>>> findCityCosts(
            @ApiParam(value = "나라명. 생략 시 전체")
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findCityCosts(country)));
    }

    @GetMapping("/cities/{cityCostId}")
    @ApiOperation(value = "도시 물가 단건 조회")
    public ResponseEntity<ApiResponse<CityCostResponseDTO>> findCityCost(
            @PathVariable Long cityCostId) {
        return ResponseEntity.ok(ApiResponse.success(this.service.findCityCost(cityCostId)));
    }
}
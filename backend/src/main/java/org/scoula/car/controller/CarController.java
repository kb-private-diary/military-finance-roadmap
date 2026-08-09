package org.scoula.car.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarBudgetStatusResponseDTO;
import org.scoula.car.dto.CarEvSubsidyResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarGoalResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
import org.scoula.car.dto.CarModelSelectRequestDTO;
import org.scoula.car.dto.CarRecommendationResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;
import org.scoula.car.service.CarService;
import org.scoula.common.response.ApiResponse;
import org.scoula.security.account.domain.CustomUser;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
@Log4j2
public class CarController {

    private final CarService carService;

    // CAR-API-01: 자동차 목표 신규 등록
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<CarGoalCreateResponseDTO>> createCarGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody CarGoalCreateRequestDTO requestDTO) {
        Long userId = customUser.getMember().getId();
        log.info("Creating car goal for userId: {}", userId);
        CarGoalCreateResponseDTO responseDTO = this.carService.createCarGoal(userId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    // CAR-API-03: 자동차 목표 목록 조회
    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<List<CarGoalResponseDTO>>> getCarGoals(
            @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching car goals for userId: {}", userId);
        List<CarGoalResponseDTO> responseDTO = this.carService.findCarGoals(userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-04: 자동차 목표 상세 조회
    @GetMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<CarGoalResponseDTO>> getCarGoalDetail(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching car goal detail for goalId: {}, userId: {}", goalId, userId);
        CarGoalResponseDTO responseDTO = this.carService.findCarGoalDetail(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-07: 예산/차종 기반 차량 추천 목록 조회
    // CAR-API-16: year/mileageKm을 넘기면(중고차 목표 한정) 자동추정 대신 직접 선택한 연식/키로수 기준으로 재계산
    @GetMapping("/goals/{goalId}/recommendations")
    public ResponseEntity<ApiResponse<List<CarRecommendationResponseDTO>>> getRecommendations(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer mileageKm) {
        Long userId = customUser.getMember().getId();
        log.info("Fetching car recommendations for goalId: {}, userId: {}, year: {}, mileageKm: {}",
                goalId, userId, year, mileageKm);
        List<CarRecommendationResponseDTO> responseDTO = (year != null || mileageKm != null)
                ? this.carService.recommendCarsByFilter(goalId, userId, year, mileageKm)
                : this.carService.recommendCars(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-08: 추천 목록 중 차량 모델 선택 반영
    @PatchMapping("/goals/{goalId}/select-model")
    public ResponseEntity<ApiResponse<CarGoalResponseDTO>> selectCarModel(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId, @RequestBody CarModelSelectRequestDTO requestDTO) {
        Long userId = customUser.getMember().getId();
        log.info("Selecting car model for goalId: {}, userId: {}", goalId, userId);
        CarGoalResponseDTO responseDTO = this.carService.selectCarModel(goalId, userId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-09: 오피넷 유가 연동 기반 연간 유지비(연료비+보험료) 조회
    @GetMapping("/goals/{goalId}/maintenance-cost")
    public ResponseEntity<ApiResponse<CarMaintenanceCostResponseDTO>> getMaintenanceCost(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Calculating maintenance cost for goalId: {}, userId: {}", goalId, userId);
        CarMaintenanceCostResponseDTO responseDTO = this.carService.calculateMaintenanceCost(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-10: 취득세 및 공채매입 여부 조회
    @GetMapping("/goals/{goalId}/acquisition-tax")
    public ResponseEntity<ApiResponse<CarAcquisitionTaxResponseDTO>> getAcquisitionTax(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Calculating acquisition tax for goalId: {}, userId: {}", goalId, userId);
        CarAcquisitionTaxResponseDTO responseDTO = this.carService.calculateAcquisitionTax(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-13: 중고차 시세 추정
    @GetMapping("/goals/{goalId}/used-price")
    public ResponseEntity<ApiResponse<CarUsedPriceResponseDTO>> getUsedPrice(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Calculating used price for goalId: {}, userId: {}", goalId, userId);
        CarUsedPriceResponseDTO responseDTO = this.carService.calculateUsedPrice(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-12: 지역 기준 전기차 보조금 계산
    @GetMapping("/goals/{goalId}/ev-subsidy")
    public ResponseEntity<ApiResponse<CarEvSubsidyResponseDTO>> getEvSubsidy(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Calculating EV subsidy for goalId: {}, userId: {}", goalId, userId);
        CarEvSubsidyResponseDTO responseDTO = this.carService.calculateEvSubsidy(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-14: 선택 차량 구매비용이 기준 예산을 넘는지 확인 (초과 시에만 대출상품 노출용)
    @GetMapping("/goals/{goalId}/budget-status")
    public ResponseEntity<ApiResponse<CarBudgetStatusResponseDTO>> getBudgetStatus(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Checking budget status for goalId: {}, userId: {}", goalId, userId);
        CarBudgetStatusResponseDTO responseDTO = this.carService.checkBudgetStatus(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    // CAR-API-15: 자동차 목표 확정(완료) — status를 CONFIRMED로 전환
    @PostMapping("/goals/{goalId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        Long userId = customUser.getMember().getId();
        log.info("Confirming car goal for goalId: {}, userId: {}", goalId, userId);
        this.carService.confirmGoal(goalId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

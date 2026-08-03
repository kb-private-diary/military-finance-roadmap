package org.scoula.car.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.car.client.OpinetClient;
import org.scoula.car.domain.CarGoalVO;
import org.scoula.car.domain.CarInsuranceVO;
import org.scoula.car.domain.CarModelVO;
import org.scoula.car.domain.CarTaxVO;
import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
import org.scoula.car.dto.CarUsedPriceResponseDTO;
import org.scoula.car.mapper.CarMapper;
import org.scoula.common.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Log4j2
public class CarServiceImpl implements CarService {

    // 연간 예상 주행거리(km) 가정값
    private static final int ANNUAL_MILEAGE_KM = 12_000;

    // 차종코드별 평균 연비(km/L) 가정값
    private static final Map<Integer, Integer> FUEL_EFFICIENCY_KM_PER_LITER = Map.of(
            1, 15, // 경차
            2, 12, // 준중형
            3, 10  // SUV
    );
    private static final int DEFAULT_FUEL_EFFICIENCY_KM_PER_LITER = 12;

    // 연차별 정률감가율 가정값
    private static final BigDecimal ANNUAL_RETENTION_RATE = BigDecimal.valueOf(0.8);
    // 연식 미선택 시 가정 연차(추천 단계 등 아직 특정 연식을 고르기 전)
    private static final int DEFAULT_ASSUMED_AGE_YEARS = 3;

    private final CarMapper carMapper;
    private final OpinetClient opinetClient;

    @Override
    @Transactional
    public CarGoalCreateResponseDTO createCarGoal(CarGoalCreateRequestDTO requestDTO) {
        Long budget = requestDTO.getBudget();
        if (budget == null || budget <= 0) {
            throw BusinessException.badRequest("예산은 0보다 커야 합니다", "CAR_001");
        }

        CarGoalVO carGoalVO = new CarGoalVO();
        carGoalVO.setUserId(requestDTO.getUserId());
        carGoalVO.setBudget(requestDTO.getBudget());
        carGoalVO.setCarTypeCode(requestDTO.getCarTypeCode());
        carGoalVO.setIsNew(requestDTO.getIsNew());
        carGoalVO.setTargetDate(requestDTO.getTargetDate());
        carGoalVO.setRegion(requestDTO.getRegion());

        this.carMapper.createCarGoal(carGoalVO);

        return new CarGoalCreateResponseDTO(carGoalVO.getGoalId());
    }

    @Override
    public CarMaintenanceCostResponseDTO calculateMaintenanceCost(Long goalId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }
        if (goal.getSelectedModelId() == null) {
            throw BusinessException.badRequest("차량 모델을 먼저 선택해야 합니다", "CAR_004");
        }
        if (goal.getExperienceYears() == null) {
            throw BusinessException.badRequest("운전경력 정보가 없습니다", "CAR_005");
        }

        CarModelVO model = this.carMapper.selectCarModelById(goal.getSelectedModelId());
        String experienceBracket = this.resolveExperienceBracket(goal.getExperienceYears());
        CarInsuranceVO insurance = this.carMapper.selectInsuranceByTypeAndBracket(
                goal.getCarTypeCode(), experienceBracket);
        if (insurance == null) {
            throw BusinessException.notFound("보험료 기준 정보를 찾을 수 없습니다", "CAR_006");
        }

        String prodCode = this.resolveProdCode(model.getFuelType());
        BigDecimal pricePerLiter = this.opinetClient.fetchAvgPricePerLiter(prodCode);
        int fuelEfficiency = FUEL_EFFICIENCY_KM_PER_LITER.getOrDefault(
                goal.getCarTypeCode(), DEFAULT_FUEL_EFFICIENCY_KM_PER_LITER);

        BigDecimal annualLiters = BigDecimal.valueOf(ANNUAL_MILEAGE_KM)
                .divide(BigDecimal.valueOf(fuelEfficiency), 4, RoundingMode.HALF_UP);
        long fuelCostAnnualWon = annualLiters.multiply(pricePerLiter)
                .setScale(0, RoundingMode.HALF_UP).longValue();
        long fuelCostAnnualManwon = Math.round(fuelCostAnnualWon / 10_000.0);

        return CarMaintenanceCostResponseDTO.builder()
                .goalId(goalId)
                .fuelType(model.getFuelType())
                .fuelPricePerLiter(pricePerLiter.setScale(0, RoundingMode.HALF_UP).longValue())
                .annualMileageKm(ANNUAL_MILEAGE_KM)
                .fuelEfficiencyKmPerLiter(fuelEfficiency)
                .estimatedFuelCostAnnual(fuelCostAnnualManwon)
                .insurancePremiumMin(insurance.getEstimatedPremiumMin())
                .insurancePremiumMax(insurance.getEstimatedPremiumMax())
                .totalMaintenanceCostMin(fuelCostAnnualManwon + insurance.getEstimatedPremiumMin())
                .totalMaintenanceCostMax(fuelCostAnnualManwon + insurance.getEstimatedPremiumMax())
                .build();
    }

    @Override
    public CarAcquisitionTaxResponseDTO calculateAcquisitionTax(Long goalId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }
        if (goal.getSelectedModelId() == null) {
            throw BusinessException.badRequest("차량 모델을 먼저 선택해야 합니다", "CAR_004");
        }

        CarModelVO model = this.carMapper.selectCarModelById(goal.getSelectedModelId());
        CarTaxVO tax = this.carMapper.selectTaxByTypeCode(goal.getCarTypeCode());
        if (tax == null) {
            throw BusinessException.notFound("취득세 기준 정보를 찾을 수 없습니다", "CAR_007");
        }

        long acquisitionTaxAmount = BigDecimal.valueOf(model.getBasePrice())
                .multiply(tax.getAcquisitionTaxRate())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValue();

        // cc 데이터 없어 경차만 공채면제로 근사
        boolean bondExempt = goal.getCarTypeCode() != null && goal.getCarTypeCode() == 1;

        return CarAcquisitionTaxResponseDTO.builder()
                .goalId(goalId)
                .vehiclePrice(model.getBasePrice())
                .acquisitionTaxRate(tax.getAcquisitionTaxRate())
                .acquisitionTaxAmount(acquisitionTaxAmount)
                .bondExemptEngineCc(tax.getBondExemptEngineCc())
                .bondExempt(bondExempt)
                .build();
    }

    @Override
    public CarUsedPriceResponseDTO calculateUsedPrice(Long goalId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }
        if (goal.getSelectedModelId() == null) {
            throw BusinessException.badRequest("차량 모델을 먼저 선택해야 합니다", "CAR_004");
        }

        CarModelVO model = this.carMapper.selectCarModelById(goal.getSelectedModelId());
        CarTaxVO tax = this.carMapper.selectTaxByTypeCode(goal.getCarTypeCode());
        if (tax == null) {
            throw BusinessException.notFound("취득세 기준 정보를 찾을 수 없습니다", "CAR_007");
        }

        int ageYears = goal.getSelectedYear() == null
                ? DEFAULT_ASSUMED_AGE_YEARS
                : Math.max(0, LocalDate.now().getYear() - goal.getSelectedYear());
        long estimatedUsedPrice = BigDecimal.valueOf(model.getBasePrice())
                .multiply(ANNUAL_RETENTION_RATE.pow(ageYears))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        long acquisitionTaxAmount = BigDecimal.valueOf(estimatedUsedPrice)
                .multiply(tax.getAcquisitionTaxRate())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValue();

        return CarUsedPriceResponseDTO.builder()
                .goalId(goalId)
                .modelName(model.getModelName())
                .baseNewPrice(model.getBasePrice())
                .selectedYear(goal.getSelectedYear())
                .ageYears(ageYears)
                .estimatedUsedPrice(estimatedUsedPrice)
                .acquisitionTaxAmount(acquisitionTaxAmount)
                .totalPrice(estimatedUsedPrice + acquisitionTaxAmount)
                .build();
    }

    // 운전경력(년) → car_insurance.experience_bracket 구간 문자열 변환
    private String resolveExperienceBracket(int experienceYears) {
        if (experienceYears < 3) {
            return "3년미만";
        }
        if (experienceYears < 5) {
            return "3~5년";
        }
        return "5년이상";
    }

    // fuel_type → 오피넷 유종 코드 변환
    private String resolveProdCode(String fuelType) {
        if ("경유".equals(fuelType)) {
            return OpinetClient.PRODCD_DIESEL;
        }
        if ("LPG".equals(fuelType)) {
            return OpinetClient.PRODCD_LPG;
        }
        return OpinetClient.PRODCD_GASOLINE;
    }
}

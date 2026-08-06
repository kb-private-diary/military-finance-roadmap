package org.scoula.car.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.car.client.OpinetClient;
import org.scoula.car.domain.CarEvVO;
import org.scoula.car.domain.CarGoalVO;
import org.scoula.car.domain.CarInsuranceVO;
import org.scoula.car.domain.CarModelVO;
import org.scoula.car.domain.CarTaxPrepayVO;
import org.scoula.car.domain.CarTaxVO;
import org.scoula.car.dto.CarAcquisitionTaxResponseDTO;
import org.scoula.car.dto.CarEvSubsidyResponseDTO;
import org.scoula.car.dto.CarGoalCreateRequestDTO;
import org.scoula.car.dto.CarGoalCreateResponseDTO;
import org.scoula.car.dto.CarGoalResponseDTO;
import org.scoula.car.dto.CarMaintenanceCostResponseDTO;
import org.scoula.car.dto.CarModelSelectRequestDTO;
import org.scoula.car.dto.CarRecommendationResponseDTO;
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

    // 전기차 평균 전비(km/kWh), 공용 충전 평균단가(원/kWh) 가정값
    private static final double EV_EFFICIENCY_KM_PER_KWH = 5.5;
    private static final long ELECTRICITY_PRICE_WON_PER_KWH = 250;

    // 차종코드별 연간 자동차세 기준액(만원) 가정값
    private static final Map<Integer, Long> ANNUAL_VEHICLE_TAX_BASE_MANWON = Map.of(
            1, 10L, // 경차
            2, 29L, // 준중형
            3, 52L  // SUV
    );
    private static final long DEFAULT_ANNUAL_VEHICLE_TAX_BASE_MANWON = 29L;

    // 연차별 정률감가율 가정값
    private static final BigDecimal ANNUAL_RETENTION_RATE = BigDecimal.valueOf(0.8);
    private static final double ANNUAL_RETENTION_RATE_DOUBLE = 0.8;
    // 연식 미선택 시 가정 연차(목표에 예산이 없는 등 추정 불가한 경우의 기본값)
    private static final int DEFAULT_ASSUMED_AGE_YEARS = 3;
    // 예산 맞춤 연식 추정 시 허용하는 최대 연차
    // 실제 중고차는 아무리 오래돼도 신차가 대비 일정 비율(약 40%) 밑으로는 잘 안 떨어지므로,
    // 그 이상 연차를 가정해도 의미가 없다고 보고 상한을 4년으로 제한한다 (0.8^4 ≈ 41%)
    private static final int MAX_ASSUMED_AGE_YEARS = 4;
    // 추천 목록에서 허용하는 예산 초과 허용 오차(만원) — 이보다 많이 넘는 차량은 목록에서 제외
    private static final long BUDGET_OVERFLOW_TOLERANCE_MANWON = 100L;

    private final CarMapper carMapper;
    private final OpinetClient opinetClient;

    @Override
    @Transactional
    public CarGoalCreateResponseDTO createCarGoal(Long userId, CarGoalCreateRequestDTO requestDTO) {
        Long budget = requestDTO.getBudget();
        if (budget == null || budget <= 0) {
            throw BusinessException.badRequest("예산은 0보다 커야 합니다", "CAR_001");
        }

        CarGoalVO carGoalVO = new CarGoalVO();
        carGoalVO.setUserId(userId);
        carGoalVO.setBudget(requestDTO.getBudget());
        carGoalVO.setIsNew(requestDTO.getIsNew());
        carGoalVO.setExperienceYears(requestDTO.getExperienceYears());
        carGoalVO.setTargetDate(requestDTO.getTargetDate());
        carGoalVO.setRegion(requestDTO.getRegion());

        this.carMapper.createCarGoal(carGoalVO);

        return new CarGoalCreateResponseDTO(carGoalVO.getGoalId());
    }

    @Override
    public List<CarGoalResponseDTO> findCarGoals(Long userId) {
        return this.carMapper.selectCarGoalsByUserId(userId);
    }

    @Override
    public CarGoalResponseDTO findCarGoalDetail(Long goalId, Long userId) {
        CarGoalResponseDTO goal = this.carMapper.selectCarGoalDetailById(goalId, userId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }
        return goal;
    }

    @Override
    public List<CarRecommendationResponseDTO> recommendCars(Long goalId, Long userId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId, userId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }

        // 목표 단계에서는 차종을 특정하지 않으므로 경차/준중형/SUV 전체 후보를 반환한다.
        // (차종별 취득세율이 달라 후보마다 자기 차종 기준으로 계산해야 함)
        List<CarModelVO> candidates = this.carMapper.selectAllCarModels();
        Map<Integer, CarTaxVO> taxByType = new HashMap<>();
        boolean isNew = Boolean.TRUE.equals(goal.getIsNew());
        int currentYear = LocalDate.now().getYear();

        List<CarRecommendationResponseDTO> recommendations = new ArrayList<>();
        for (CarModelVO model : candidates) {
            CarTaxVO tax = taxByType.computeIfAbsent(
                    model.getCarTypeCode(), this.carMapper::selectTaxByTypeCode);
            if (tax == null) {
                throw BusinessException.notFound("취득세 기준 정보를 찾을 수 없습니다", "CAR_007");
            }

            Integer assumedYear = null;
            long estimatedPrice;
            if (isNew) {
                estimatedPrice = model.getBasePrice();
            } else {
                int age = this.estimateAgeFittingBudget(
                        model.getBasePrice(), tax.getAcquisitionTaxRate(), goal.getBudget());
                assumedYear = currentYear - age;
                estimatedPrice = BigDecimal.valueOf(model.getBasePrice())
                        .multiply(ANNUAL_RETENTION_RATE.pow(age))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue();
            }
            long acquisitionTaxAmount = BigDecimal.valueOf(estimatedPrice)
                    .multiply(tax.getAcquisitionTaxRate())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    .longValue();
            long totalPrice = estimatedPrice + acquisitionTaxAmount;

            recommendations.add(CarRecommendationResponseDTO.builder()
                    .modelId(model.getModelId())
                    .manufacturer(model.getManufacturer())
                    .modelName(model.getModelName())
                    .carTypeCode(model.getCarTypeCode())
                    .fuelType(model.getFuelType())
                    .baseNewPrice(model.getBasePrice())
                    .assumedYear(assumedYear)
                    .estimatedPrice(estimatedPrice)
                    .acquisitionTaxAmount(acquisitionTaxAmount)
                    .totalPrice(totalPrice)
                    .withinBudget(goal.getBudget() != null && totalPrice <= goal.getBudget())
                    .build());
        }

        // 예산을 크게 벗어나는 차량은 추천 목록에서 아예 제외한다.
        // 단, 예산을 살짝 넘는 차량은 "예산 초과" 배지를 단 채로 계속 보여준다 (허용 오차: +100만원).
        if (goal.getBudget() != null) {
            long maxAllowedPrice = goal.getBudget() + BUDGET_OVERFLOW_TOLERANCE_MANWON;
            recommendations.removeIf(item -> item.getTotalPrice() > maxAllowedPrice);
        }

        recommendations.sort(Comparator.comparingLong(CarRecommendationResponseDTO::getTotalPrice));
        return recommendations;
    }

    @Override
    @Transactional
    public CarGoalResponseDTO selectCarModel(Long goalId, Long userId, CarModelSelectRequestDTO requestDTO) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId, userId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }
        if (requestDTO.getModelId() == null) {
            throw BusinessException.badRequest("선택할 차량 모델을 지정해야 합니다", "CAR_008");
        }

        CarModelVO model = this.carMapper.selectCarModelById(requestDTO.getModelId());
        if (model == null) {
            throw BusinessException.notFound("선택한 차량 모델을 찾을 수 없습니다", "CAR_009");
        }

        // 목표 단계에서는 차종을 특정하지 않으므로, 실제 선택한 차량의 차종을 목표에 반영한다.
        goal.setCarTypeCode(model.getCarTypeCode());
        goal.setSelectedModelId(model.getModelId());
        goal.setSelectedYear(requestDTO.getSelectedYear());
        goal.setStatus("SELECTED");

        this.carMapper.updateSelectedModel(goal);

        return this.findCarGoalDetail(goalId, userId);
    }

    @Override
    public CarMaintenanceCostResponseDTO calculateMaintenanceCost(Long goalId, Long userId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId, userId);
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

        boolean isElectric = "전기".equals(model.getFuelType());
        long fuelPricePerUnit;
        int fuelEfficiency;
        long fuelCostAnnualManwon;
        if (isElectric) {
            fuelPricePerUnit = ELECTRICITY_PRICE_WON_PER_KWH;
            fuelEfficiency = (int) Math.round(EV_EFFICIENCY_KM_PER_KWH);
            double annualKwh = ANNUAL_MILEAGE_KM / EV_EFFICIENCY_KM_PER_KWH;
            fuelCostAnnualManwon = Math.round(annualKwh * ELECTRICITY_PRICE_WON_PER_KWH / 10_000.0);
        } else {
            String prodCode = this.resolveProdCode(model.getFuelType());
            BigDecimal pricePerLiter = this.opinetClient.fetchAvgPricePerLiter(prodCode);
            fuelEfficiency = FUEL_EFFICIENCY_KM_PER_LITER.getOrDefault(
                    goal.getCarTypeCode(), DEFAULT_FUEL_EFFICIENCY_KM_PER_LITER);

            BigDecimal annualLiters = BigDecimal.valueOf(ANNUAL_MILEAGE_KM)
                    .divide(BigDecimal.valueOf(fuelEfficiency), 4, RoundingMode.HALF_UP);
            long fuelCostAnnualWon = annualLiters.multiply(pricePerLiter)
                    .setScale(0, RoundingMode.HALF_UP).longValue();
            fuelPricePerUnit = pricePerLiter.setScale(0, RoundingMode.HALF_UP).longValue();
            fuelCostAnnualManwon = Math.round(fuelCostAnnualWon / 10_000.0);
        }

        long vehicleTaxBase = ANNUAL_VEHICLE_TAX_BASE_MANWON.getOrDefault(
                goal.getCarTypeCode(), DEFAULT_ANNUAL_VEHICLE_TAX_BASE_MANWON);
        CarTaxPrepayVO prepay = this.carMapper.selectBestPrepayDiscount(LocalDate.now().getYear());
        BigDecimal prepayDiscountRate = prepay == null ? BigDecimal.ZERO : prepay.getDiscountRate();
        long vehicleTaxAfterDiscount = BigDecimal.valueOf(vehicleTaxBase)
                .multiply(BigDecimal.ONE.subtract(prepayDiscountRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        long totalMin = fuelCostAnnualManwon + vehicleTaxAfterDiscount + insurance.getEstimatedPremiumMin();
        long totalMax = fuelCostAnnualManwon + vehicleTaxAfterDiscount + insurance.getEstimatedPremiumMax();

        return CarMaintenanceCostResponseDTO.builder()
                .goalId(goalId)
                .fuelType(model.getFuelType())
                .fuelPricePerLiter(fuelPricePerUnit)
                .annualMileageKm(ANNUAL_MILEAGE_KM)
                .fuelEfficiencyKmPerLiter(fuelEfficiency)
                .estimatedFuelCostAnnual(fuelCostAnnualManwon)
                .annualVehicleTaxBase(vehicleTaxBase)
                .prepayDiscountRate(prepayDiscountRate)
                .annualVehicleTaxAfterDiscount(vehicleTaxAfterDiscount)
                .insurancePremiumMin(insurance.getEstimatedPremiumMin())
                .insurancePremiumMax(insurance.getEstimatedPremiumMax())
                .totalMaintenanceCostMin(totalMin)
                .totalMaintenanceCostMax(totalMax)
                .totalMaintenanceCost3YearMin(totalMin * 3)
                .totalMaintenanceCost3YearMax(totalMax * 3)
                .build();
    }

    @Override
    public CarAcquisitionTaxResponseDTO calculateAcquisitionTax(Long goalId, Long userId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId, userId);
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
    public CarUsedPriceResponseDTO calculateUsedPrice(Long goalId, Long userId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId, userId);
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

    @Override
    public CarEvSubsidyResponseDTO calculateEvSubsidy(Long goalId, Long userId) {
        CarGoalVO goal = this.carMapper.selectCarGoalById(goalId, userId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다", "CAR_003");
        }
        if (goal.getSelectedModelId() == null) {
            throw BusinessException.badRequest("차량 모델을 먼저 선택해야 합니다", "CAR_004");
        }

        CarModelVO model = this.carMapper.selectCarModelById(goal.getSelectedModelId());
        if (!"전기".equals(model.getFuelType())) {
            throw BusinessException.badRequest("선택한 차량은 전기차가 아닙니다", "CAR_011");
        }

        CarEvVO ev = this.carMapper.selectEvSubsidyByRegion(goal.getRegion());
        if (ev == null) {
            throw BusinessException.notFound("해당 지역의 전기차 보조금 정보를 찾을 수 없습니다", "CAR_012");
        }

        long totalSubsidy = ev.getNationalSubsidy() + ev.getLocalSubsidy();

        return CarEvSubsidyResponseDTO.builder()
                .goalId(goalId)
                .modelName(model.getModelName())
                .basePrice(model.getBasePrice())
                .region(ev.getRegion())
                .nationalSubsidy(ev.getNationalSubsidy())
                .localSubsidy(ev.getLocalSubsidy())
                .totalSubsidy(totalSubsidy)
                .baseYear(ev.getBaseYear())
                .finalPrice(Math.max(0, model.getBasePrice() - totalSubsidy))
                .build();
    }

    // 예산 안에서 가장 최신 연식(연차가 가장 적은)을 추정 — 신차가가 이미 예산 이내면 0년(연식 그대로)
    private int estimateAgeFittingBudget(long basePrice, BigDecimal acquisitionTaxRate, Long budget) {
        if (budget == null) {
            return DEFAULT_ASSUMED_AGE_YEARS;
        }

        double taxMultiplier = 1 + acquisitionTaxRate.doubleValue() / 100.0;
        double targetPrice = budget / taxMultiplier;
        double ratio = targetPrice / basePrice;
        if (ratio >= 1.0) {
            return 0;
        }

        int age = (int) Math.ceil(Math.log(ratio) / Math.log(ANNUAL_RETENTION_RATE_DOUBLE));
        return Math.max(0, Math.min(MAX_ASSUMED_AGE_YEARS, age));
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

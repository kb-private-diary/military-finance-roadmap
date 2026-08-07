package org.scoula.rent.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.AreaMgmtAnchorVO;
import org.scoula.rent.domain.AreaUsageAnchorVO;
import org.scoula.rent.domain.MonthCoefVO;
import org.scoula.rent.domain.RegionMgmtFeeVO;
import org.scoula.rent.domain.RegionUtilityVO;
import org.scoula.rent.domain.SnapshotVO;
import org.scoula.rent.dto.MonthlyUtilityDTO;
import org.scoula.rent.dto.UtilityConfigResponseDTO;
import org.scoula.rent.dto.UtilityEstimateResponseDTO;
import org.scoula.rent.mapper.UtilityMapper;
import org.scoula.rent.util.UtilityCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 공과금·관리비 서비스 구현
 *
 * 계산 원칙 (명세 v3.5)
 * 1. 면적 앵커(area_usage_anchor / area_mgmt_anchor)를 DB에서 조회해 UtilityCalculator.interpolate로 보간
 *    - UtilityCalculator.baseKwh/baseHeat/calcMgmtFee는 내부 하드코딩 앵커를 쓰므로 직접 호출하지 않는다
 *    - 요금 개정 시 DB UPDATE만으로 반영되도록 조회값을 주입해 계산
 * 2. 총액은 월비용 × 개월수가 아니라 입주 월부터 순차 월별 누적 (겨울 포함 횟수에 따라 비선형)
 * 3. 관리비는 공용관리비만 계산 (개별사용료는 전기·수도·난방에서 별도 계산 - 이중계산 방지)
 */
@Service
@RequiredArgsConstructor
public class UtilityServiceImpl implements UtilityService {

    private final UtilityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public UtilityConfigResponseDTO getConfig(String regionCode) {
        RegionUtilityVO region = mapper.findRegionUtility(regionCode);
        if (region == null) {
            throw BusinessException.notFound("해당 지역의 공과금 계수를 찾을 수 없습니다.", "RENT_UTIL_001");
        }
        return UtilityConfigResponseDTO.builder()
                .regionUtility(region)
                .regionMgmtFee(mapper.findRegionMgmtFee(regionCode))
                .usageAnchors(mapper.findAreaUsageAnchors())
                .mgmtAnchors(mapper.findAreaMgmtAnchors())
                .monthCoefs(mapper.findAllMonthCoef())
                .electricRates(mapper.findElectricRates())
                .constants(toConstantMap(mapper.findConstants()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UtilityEstimateResponseDTO estimate(String regionCode, double areaSqm,
                                               int startYear, int startMonth, int months) {
        validateInput(areaSqm, startMonth, months);

        RegionUtilityVO region = mapper.findRegionUtility(regionCode);
        if (region == null) {
            throw BusinessException.notFound("해당 지역의 공과금 계수를 찾을 수 없습니다.", "RENT_UTIL_001");
        }

        List<MonthlyUtilityDTO> monthly = calcMonthly(region, regionCode, areaSqm, startYear, startMonth, months);
        int total = monthly.stream().mapToInt(MonthlyUtilityDTO::getTotalFee).sum();

        return UtilityEstimateResponseDTO.of(
                region.getSidoName(), areaSqm, formatYm(startYear, startMonth),
                months, total, region.getHeatSample(), monthly);
    }

    @Override
    @Transactional
    public void saveSnapshot(Long roadmapId, String regionCode, double areaSqm,
                             int startYear, int startMonth, int months) {
        validateInput(areaSqm, startMonth, months);

        RegionUtilityVO region = mapper.findRegionUtility(regionCode);
        if (region == null) {
            throw BusinessException.notFound("해당 지역의 공과금 계수를 찾을 수 없습니다.", "RENT_UTIL_001");
        }

        List<MonthlyUtilityDTO> monthly = calcMonthly(region, regionCode, areaSqm, startYear, startMonth, months);

        // 저장 당시 요금표 기준일 - region_utility.effective_from을 스냅샷 기준일로 사용
        LocalDate rateBaseDt = region.getEffectiveFrom();

        List<SnapshotVO> rows = new ArrayList<>();
        for (MonthlyUtilityDTO m : monthly) {
            SnapshotVO vo = new SnapshotVO();
            vo.setRoadmapId(roadmapId);
            vo.setMonthSeq(m.getMonthSeq());
            vo.setCalendarYm(m.getCalendarYm());
            vo.setElecKwh(m.getElecKwh());
            vo.setElecFee(m.getElecFee());
            vo.setHeatFee(m.getHeatFee());
            vo.setWaterFee(m.getWaterFee());
            vo.setMgmtFee(m.getMgmtFee());
            vo.setTotalFee(m.getTotalFee());
            vo.setRateBaseDt(rateBaseDt);
            rows.add(vo);
        }
        mapper.insertSnapshot(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnapshotVO> findSnapshot(Long roadmapId) {
        return mapper.findSnapshotByRoadmap(roadmapId);
    }

    /**
     * Step3 매물 상세용 공용관리비 (면적앵커 보간 단가 × 전용면적 × 시도계수)
     * Step5 월별 계산의 관리비 부분과 동일 로직 - 폐기된 region_fee_stat를 대체
     */
    @Override
    @Transactional(readOnly = true)
    public int calcManagementFee(String regionCode, double areaSqm) {
        if (areaSqm <= 0) {
            return 0;
        }
        // 관리비 지역계수 (표본 부족 지역은 인접 권역 평균이 DB에 반영됨, 없으면 전국 1.0)
        RegionMgmtFeeVO mgmt = mapper.findRegionMgmtFee(regionCode);
        double mgmtCoef = mgmt != null ? mgmt.getMgmtCoef() : 1.0;
        // 면적앵커 보간 단가(원/㎡) - 계단식 금지, 선형보간
        List<AreaMgmtAnchorVO> anchors = mapper.findAreaMgmtAnchors();
        double[] mArea = anchors.stream().mapToDouble(AreaMgmtAnchorVO::getAreaSqm).toArray();
        double[] mRate = anchors.stream().mapToDouble(AreaMgmtAnchorVO::getFeePerSqm).toArray();
        double perSqm = UtilityCalculator.interpolate(mArea, mRate, areaSqm);
        return (int) Math.round(perSqm * areaSqm * mgmtCoef);
    }

    // ------------------------------------------------------------------
    //  내부 계산
    // ------------------------------------------------------------------

    /**
     * 거주 N개월 월별 계산 (입주 월부터 순차)
     * 면적 앵커·시도 계수·월별 계수를 DB에서 조회해 주입
     */
    private List<MonthlyUtilityDTO> calcMonthly(RegionUtilityVO region, String regionCode, double areaSqm,
                                                int startYear, int startMonth, int months) {
        // 관리비 지역계수 (표본 부족 지역은 인접 권역 평균이 이미 DB에 반영됨, 없으면 전국 1.0)
        RegionMgmtFeeVO mgmt = mapper.findRegionMgmtFee(regionCode);
        double mgmtCoef = mgmt != null ? mgmt.getMgmtCoef() : 1.0;

        // 면적 앵커 조회 → 보간용 배열 (계단식 금지, 선형보간)
        List<AreaUsageAnchorVO> usage = mapper.findAreaUsageAnchors();
        List<AreaMgmtAnchorVO> mgmtAnchors = mapper.findAreaMgmtAnchors();

        double[] uArea = usage.stream().mapToDouble(AreaUsageAnchorVO::getAreaSqm).toArray();
        double[] uKwh = usage.stream().mapToDouble(AreaUsageAnchorVO::getElecKwh).toArray();
        double[] uHeat = usage.stream().mapToDouble(AreaUsageAnchorVO::getHeatFee).toArray();
        double[] mArea = mgmtAnchors.stream().mapToDouble(AreaMgmtAnchorVO::getAreaSqm).toArray();
        double[] mRate = mgmtAnchors.stream().mapToDouble(AreaMgmtAnchorVO::getFeePerSqm).toArray();

        // 면적 → 기준값 (면적은 거주기간 내내 고정이므로 한 번만 보간)
        double baseKwh = UtilityCalculator.interpolate(uArea, uKwh, areaSqm);
        double baseHeat = UtilityCalculator.interpolate(uArea, uHeat, areaSqm);
        double perSqm = UtilityCalculator.interpolate(mArea, mRate, areaSqm);
        int mgmtFee = (int) Math.round(perSqm * areaSqm * mgmtCoef); // 공용관리비는 계절 무관 - 매월 동일

        // 월별 계수 (1~12) 조회
        Map<Integer, MonthCoefVO> monthCoef = new LinkedHashMap<>();
        for (MonthCoefVO mc : mapper.findAllMonthCoef()) {
            monthCoef.put(mc.getMonth(), mc);
        }

        // 입주 월부터 순차로 N개월 계산
        List<MonthlyUtilityDTO> result = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            int offset = startMonth - 1 + i;
            int calMonth = (offset % 12) + 1;      // 달력 월 1~12
            int calYear = startYear + offset / 12; // 연도 (해 넘김 반영)
            MonthCoefVO mc = monthCoef.get(calMonth);

            // 전기: 누진요금표 × (면적기준 kWh × 시도계수 × 월별계수)
            double kwh = baseKwh * region.getElecCoef() * mc.getElecCoef();
            int electric = UtilityCalculator.calcElectric(kwh, calMonth);
            // 난방: 면적기준액 × 시도계수 × 월별계수 (실요금 이미 반영된 기준액)
            int heating = UtilityCalculator.calcHeating((int) Math.round(baseHeat),
                    region.getHeatCoef(), mc.getHeatCoef());
            // 수도: 1인 월사용량 × (상수도 + 하수도 + 물이용부담금)
            int water = UtilityCalculator.calcWater(region.getWaterUsageM3(),
                    region.getWaterRate(), region.getSewerRate());

            // Result로 합계를 위임 (total = 전기+난방+수도+관리비)
            UtilityCalculator.Result r =
                    new UtilityCalculator.Result(calMonth, kwh, electric, heating, water, mgmtFee);
            result.add(MonthlyUtilityDTO.of(i + 1, formatYm(calYear, calMonth), r));
        }
        return result;
    }

    /** 공통 상수 목록(List<Map>) → const_key → const_value 맵 */
    private Map<String, Object> toConstantMap(List<Map<String, Object>> rows) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("const_key")), row.get("const_value"));
        }
        return map;
    }

    /** 입력 검증 (validator 미탑재 - 서비스에서 BusinessException으로 처리) */
    private void validateInput(double areaSqm, int startMonth, int months) {
        if (areaSqm <= 0) {
            throw BusinessException.badRequest("전용면적이 올바르지 않습니다.", "RENT_UTIL_002");
        }
        if (startMonth < 1 || startMonth > 12) {
            throw BusinessException.badRequest("입주 월이 올바르지 않습니다.", "RENT_UTIL_003");
        }
        if (months <= 0) {
            throw BusinessException.badRequest("거주 개월수가 올바르지 않습니다.", "RENT_UTIL_004");
        }
    }

    /** 연·월 → yyyy-MM */
    private String formatYm(int year, int month) {
        return String.format("%04d-%02d", year, month);
    }
}

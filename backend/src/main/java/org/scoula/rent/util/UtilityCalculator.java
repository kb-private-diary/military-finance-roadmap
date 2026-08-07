package org.scoula.rent.util;

/**
 * 공과금·관리비 계산기
 *
 * [계산 구조]
 *   전기   = 한전 누진요금표 × (면적기준kWh × 시도계수 × 월별계수)
 *   난방   = 면적기준액 × 시도계수 × 월별계수      ※ 요금이 이미 반영된 금액
 *   수도   = 1인월사용량 × (상수도 + 하수도 + 물이용부담금)
 *   관리비 = 27,857원 × 시도계수 × 면적계수
 *
 * [데이터 출처]
 *   전기요금  한국전력 주택용전력(저압) 2026 현행
 *   난방      도시가스(도시가스협회 2026.08.01) + LPG·등유(페트로넷)
 *             + 지역난방(한국지역난방공사 112.32원/Mcal)
 *             × HEPS 14차 마이크로데이터 가구별 실측 사용량
 *   상하수도  환경부 상수도통계/하수도통계 2024
 *   관리비    K-apt 2026.01~06 + 국토부 주거실태조사 2024
 *
 * 주의: 모든 계수·요금표 값은 DB에서 조회해 주입할 것.
 *       아래 상수는 폴백 겸 문서화 목적.
 */
public class UtilityCalculator {

    /* 전기 (전국 단일 요금제) */
    private static final double ELEC_CLIMATE_FEE   = 9.0;
    private static final double ELEC_FUEL_ADJ      = 5.0;
    private static final double ELEC_VAT_FUND_RATE = 1.137;
    private static final int    ELEC_TV_FEE        = 2500;

    /* 상하수도 */
    private static final double WATER_LEVY = 170.0;

    /* 면적 앵커 (area_usage_anchor) — 선형보간용. DB에서 조회해 주입할 것 */
    private static final double[] ANCHOR_AREA = { 28.0,  59.0,  85.0, 111.0, 149.0};
    private static final double[] ANCHOR_KWH  = {161.70, 254.80, 281.50, 289.00, 340.50};
    private static final double[] ANCHOR_HEAT = {31024, 60066, 65299, 68537, 69727};

    /* 관리비 면적 앵커 (area_mgmt_anchor) — 원/㎡ */
    private static final double[] MGMT_AREA = {  42.0,   46.1,   49.8,   53.9,   58.6};
    private static final double[] MGMT_RATE = {1082.6, 1001.9, 1001.0,  984.5,  927.2};

    /**
     * 앵커 배열 선형보간. 범위 밖은 양 끝 앵커값으로 고정(clamp).
     * 구간(계단식) 방식은 경계에서 금액이 급등/역전하여 사용하지 않는다.
     */
    public static double interpolate(double[] xs, double[] ys, double x) {
        if (x <= xs[0])              return ys[0];
        if (x >= xs[xs.length - 1])  return ys[ys.length - 1];
        for (int i = 0; i < xs.length - 1; i++) {
            if (x <= xs[i + 1]) {
                double r = (x - xs[i]) / (xs[i + 1] - xs[i]);
                return ys[i] + (ys[i + 1] - ys[i]) * r;
            }
        }
        return ys[ys.length - 1];
    }

    /** 전용면적 → 월 전기 사용량 기준값 (kWh) */
    public static double baseKwh(double areaSqm) {
        return interpolate(ANCHOR_AREA, ANCHOR_KWH, areaSqm);
    }

    /** 전용면적 → 월 난방비 기준값 (원, 전국) */
    public static double baseHeat(double areaSqm) {
        return interpolate(ANCHOR_AREA, ANCHOR_HEAT, areaSqm);
    }

    /**
     * 전기요금. 7~8월은 하계 누진 구간 완화.
     */
    public static int calcElectric(double kwh, int month) {
        boolean summer = (month == 7 || month == 8);
        int t1 = summer ? 300 : 200;
        int t2 = summer ? 450 : 400;

        int    baseFee;
        double energyFee;

        if (kwh <= t1) {
            baseFee   = 910;
            energyFee = kwh * 120.0;
        } else if (kwh <= t2) {
            baseFee   = 1600;
            energyFee = t1 * 120.0 + (kwh - t1) * 214.6;
        } else {
            baseFee   = 7300;
            energyFee = t1 * 120.0 + (t2 - t1) * 214.6 + (kwh - t2) * 307.3;
        }

        double subtotal = baseFee + energyFee
                        + kwh * ELEC_CLIMATE_FEE
                        + kwh * ELEC_FUEL_ADJ;

        return (int) Math.round(subtotal * ELEC_VAT_FUND_RATE) + ELEC_TV_FEE;
    }

    /**
     * 난방비. 도시가스·LPG·등유·지역난방 실요금이 이미 반영된 기준액을 사용.
     *
     * @param baseFee   area_usage_base.heat_fee (면적 구간별 전국 기준액)
     * @param regionCoef region_utility.heat_coef
     * @param monthCoef  month_utility_coef.heat_coef
     */
    public static int calcHeating(int baseFee, double regionCoef, double monthCoef) {
        return (int) Math.round(baseFee * regionCoef * monthCoef);
    }

    /**
     * 상하수도요금. 계절 변동 미적용(편차가 작고 금액도 작음).
     */
    public static int calcWater(double m3, double waterRate, double sewerRate) {
        return (int) Math.round(m3 * (waterRate + sewerRate + WATER_LEVY));
    }

    /**
     * 관리비 (공용관리비). 개별사용료는 위 공과금에서 별도 계산됨.
     * 작은 집일수록 ㎡당 단가가 높다 — 경비·청소 인력비가 고정비이기 때문.
     *
     * @param areaSqm    전용면적 ㎡
     * @param regionCoef region_mgmt_fee.mgmt_coef
     */
    public static int calcMgmtFee(double areaSqm, double regionCoef) {
        double perSqm = interpolate(MGMT_AREA, MGMT_RATE, areaSqm);
        return (int) Math.round(perSqm * areaSqm * regionCoef);
    }

    /**
     * 월간 총액.
     *
     * @param areaSqm  전용면적 ㎡ (앵커 보간)
     * @param month    1~12
     */
    public static Result calcMonthly(
            double areaSqm,
            double elecCoef, double heatCoef,
            double monthElec, double monthHeat,
            double waterM3, double waterRate, double sewerRate,
            double mgmtRegionCoef,
            int month) {

        double kwh = baseKwh(areaSqm) * elecCoef * monthElec;

        int electric = calcElectric(kwh, month);
        int heating  = calcHeating((int) Math.round(baseHeat(areaSqm)), heatCoef, monthHeat);
        int water    = calcWater(waterM3, waterRate, sewerRate);
        int mgmt     = calcMgmtFee(areaSqm, mgmtRegionCoef);

        return new Result(month, kwh, electric, heating, water, mgmt);
    }

    public static class Result {
        public final int    month;
        public final double kwh;
        public final int    electric;
        public final int    heating;
        public final int    water;
        public final int    mgmt;
        public final int    total;

        public Result(int month, double kwh, int electric, int heating, int water, int mgmt) {
            this.month    = month;
            this.kwh      = kwh;
            this.electric = electric;
            this.heating  = heating;
            this.water    = water;
            this.mgmt     = mgmt;
            this.total    = electric + heating + water + mgmt;
        }
    }
}

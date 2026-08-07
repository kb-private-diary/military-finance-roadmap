package org.scoula.rent.domain;

/**
 * 감당도 판정 (내 재정 체크) - 총 필요자금 대비 만기금 수준
 * spec 4.3.6 / 5.4 기준: 만기금 배수로 3단계 판정
 * (Step4 부족분 카드, 추후 Step5 정밀 판정에서도 재사용)
 */
public enum RentAffordability {
    SUFFICIENT("딱 맞아요"),   // 만기금 >= 총 필요자금 * 1.2
    TIGHT("빠듯해요"),          // 총 필요자금 * 0.8 <= 만기금 < 총 필요자금 * 1.2
    OVER("예산 초과");          // 만기금 < 총 필요자금 * 0.8

    private final String label;

    RentAffordability(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    /**
     * 만기금과 총 필요자금 비교로 감당도 판정
     * 실수 곱(×1.2, ×0.8) 대신 정수 비교로 계산 (만기금×10 vs 총필요×12 / ×8)
     */
    public static RentAffordability judge(long totalRequired, long maturityAmount) {
        if (maturityAmount * 10 >= totalRequired * 12) {
            return SUFFICIENT;
        }
        if (maturityAmount * 10 >= totalRequired * 8) {
            return TIGHT;
        }
        return OVER;
    }
}

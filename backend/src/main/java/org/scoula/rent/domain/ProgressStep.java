package org.scoula.rent.domain;

/**
 * 자취 준비 진행률 5단계. 단계 코드 + 화면 라벨을 함께 정의한다.
 * 진행률 = 완료 단계 수 / 전체(5) * 100.
 *
 * <p>enum 순서 = 화면 표시 순서. 단계를 늘리려면 여기에만 추가하면 된다.</p>
 */
public enum ProgressStep {
    SAVE_GOAL("목표 저장"),
    CHECK_PRODUCT("금융상품 확인"),
    LOAN_INQUIRY("대출 사전 조회"),
    POLICY_APPLY("청년 정책 신청"),
    MOVING_BOOK("이사 업체 예약");

    private final String label;

    ProgressStep(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

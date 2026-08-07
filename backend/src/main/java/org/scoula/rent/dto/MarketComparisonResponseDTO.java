package org.scoula.rent.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 동네 시세 비교 응답 DTO (Step3 "동네 시세 상세보기")
 * 같은 법정동 + 같은 매물종류 + 유사 전용면적(±5㎡) 표본과 이 매물을 6개 항목으로 비교한다.
 *
 * <p>프론트가 필드명에 그대로 의존하므로 구조·필드명·rows 순서(6개)는 고정이다.
 * 금액(mine/avg/rentMin 등)은 원 단위 정수, area/floor 는 소수 1자리 실수, builtYear 는 연도 정수.
 */
@Data
@Builder
public class MarketComparisonResponseDTO {

    private String umdName;         // 이 매물 동 이름
    private String estateTypeLabel; // 매물종류 한글 (오피스텔 등)
    private int sampleCount;        // 비교에 쓴 유사매물 표본 수
    private boolean enough;         // 표본 충분 여부 (MIN_SAMPLE 미만이면 false → 프론트 "데이터 부족")
    private int betterCount;        // 6개 항목 중 우위 개수
    private int totalItems;         // 항목 수 (항상 6)

    private List<Row> rows;         // 항목별 비교 (아래 6개 순서 고정)

    private long rentMin;           // 월세 최저 (시세 슬라이더용)
    private long rentMax;           // 월세 최고
    private long rentAvg;           // 월세 평균
    private int rentPercentile;     // 이 매물 월세가 하위 몇 %인지 (낮을수록 저렴)

    private String verdict;         // GOOD | NORMAL | BAD
    private String verdictTitle;    // 가성비 한줄 제목
    private String verdictText;     // 가성비 설명 문장

    /**
     * 항목별 비교 한 행.
     *   mine/avg 는 항목마다 타입이 다르다(월세·보증금·㎡당월세=정수 원, 면적·층=소수1자리, 건축년도=정수).
     *   그래서 Object 로 담아 Jackson 이 값 타입 그대로(Long/Double/Integer) 직렬화하게 한다.
     */
    @Data
    @Builder
    public static class Row {
        private String key;     // monthlyRent / deposit / areaSqm / builtYear / floor / rentPerSqm
        private String label;   // 한글 라벨
        private Object mine;    // 이 매물 값
        private Object avg;     // 유사매물 평균 값
        private boolean better; // 이 매물이 우위인지 (쌀수록/클수록/최신일수록 좋음, 항목별 방향 다름)
    }
}

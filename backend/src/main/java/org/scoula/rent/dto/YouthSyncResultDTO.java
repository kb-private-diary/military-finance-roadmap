package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 온통청년 배치 동기화 결과 요약 (관리자 엔드포인트/스케줄러 로그용).
 */
@Getter
@Builder
public class YouthSyncResultDTO {
    private final int received;   // API 로 받은 주거 정책 총건수
    private final int matched;    // mclsfNm 필터 + 중복 제거 후 대상 건수
    private final int upserted;   // DB UPSERT 성공 건수
    private final int failed;     // 매핑/UPSERT 중 건너뛴(실패) 건수
}

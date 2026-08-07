package org.scoula.rent.service;

import org.scoula.rent.dto.YouthSyncResultDTO;

/**
 * 온통청년 청년 주거정책 배치 동기화 서비스 (연동명세 §4·§5).
 * API 전량 수신 → 필터·매핑·텍스트 파싱 → plcy_no 기준 UPSERT.
 */
public interface YouthPolicySyncService {

    /**
     * 온통청년 주거 정책을 받아 housing_product 테이블에 동기화한다.
     * 외부 API 실패/개별 행 오류에도 배치가 죽지 않고 요약을 반환한다.
     */
    YouthSyncResultDTO syncYouthPolicies();
}

package org.scoula.rent.service;

import java.util.List;

import org.scoula.rent.domain.SnapshotVO;
import org.scoula.rent.dto.UtilityConfigResponseDTO;
import org.scoula.rent.dto.UtilityEstimateResponseDTO;

/**
 * 공과금·관리비 서비스
 * Mapper로 계수·요금표·앵커를 조회해 DB값을 주입한 뒤 UtilityCalculator로 계산한다
 * UtilityCalculator의 하드코딩 상수(ANCHOR_*, MGMT_*)는 폴백·문서용이며 실제 계산은 DB 조회값을 쓴다
 */
public interface UtilityService {

    // Step5 진입 시 계수·요금표·앵커 일괄 조회 (프론트 캐싱용)
    UtilityConfigResponseDTO getConfig(String regionCode);

    // 거주 N개월 월별 공과금·관리비 계산 (입주 월부터 순차, 총액은 월별 누적)
    UtilityEstimateResponseDTO estimate(String regionCode, double areaSqm,
                                        int startYear, int startMonth, int months);

    // 로드맵 저장 시 월별 스냅샷 INSERT (Step4 → Step5 확정 흐름에서 호출)
    void saveSnapshot(Long roadmapId, String regionCode, double areaSqm,
                      int startYear, int startMonth, int months);

    // 로드맵 저장된 월별 스냅샷 조회 (Step5)
    List<SnapshotVO> findSnapshot(Long roadmapId);

    // Step3 매물 상세용 공용관리비 계산 (면적앵커 보간 단가 × 전용면적 × 시도계수) - region_fee_stat 대체
    int calcManagementFee(String regionCode, double areaSqm);
}

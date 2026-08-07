package org.scoula.openbanking.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.openbanking.domain.IncomeVO;

/**
 * 수입 저장 매퍼 (오픈뱅킹 연동 시 급여 등 입금 내역 income INSERT 전용)
 * 조회는 각 도메인(진로·여행·자동차·후회소비)이 자체 매퍼로 담당, 여기선 저장만
 */
public interface IncomeWriteMapper {
    // 수입 저장 (INSERT 후 income_id 채워짐)
    void insertIncome(IncomeVO income);

    // 회원의 기존 급여(SALARY) 소프트 삭제 (재동기화 시 중복 방지 위해 정리 후 재적재)
    int deleteSalariesByUserId(@Param("userId") Long userId, @Param("actor") String actor);
}

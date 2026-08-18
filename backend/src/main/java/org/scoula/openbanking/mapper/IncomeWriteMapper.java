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

    // 회원의 기존 급여(SALARY) 소프트 삭제 (급여 전체 리셋이 필요할 때 사용)
    int deleteSalariesByUserId(@Param("userId") Long userId, @Param("actor") String actor);

    // 해당 회원의 특정 연월에 급여(SALARY)가 이미 있는지 (월급 배치 멱등 - 중복 적재 방지)
    boolean existsSalaryInYearMonth(@Param("userId") Long userId,
                                    @Param("year") int year, @Param("month") int month);
}

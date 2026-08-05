package org.scoula.openbanking.mapper;

import org.scoula.openbanking.domain.SavingAccountVO;
import org.scoula.openbanking.domain.SavingHistoryVO;

/**
 * 적금 저장 매퍼 (오픈뱅킹 연동 시 saving_account/saving_history INSERT 전용)
 * 조회·만기금 계산은 석윤 saving/dashboard가 담당, 여기선 저장만
 */
public interface SavingWriteMapper {
    // 적금 계좌 저장 (INSERT 후 account_id 채워짐)
    void insertSavingAccount(SavingAccountVO account);

    // 적금 납입내역 저장 (회차별)
    void insertSavingHistory(SavingHistoryVO history);
}

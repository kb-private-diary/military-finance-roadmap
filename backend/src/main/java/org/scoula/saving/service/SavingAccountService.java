package org.scoula.saving.service;

import org.scoula.saving.dto.SavingAccountCreateRequestDTO;

public interface SavingAccountService {
    // 군적금 계좌 등록 (생성된 accountId 반환)
    Long createAccount(Long userId, String createdNm, SavingAccountCreateRequestDTO request);
}

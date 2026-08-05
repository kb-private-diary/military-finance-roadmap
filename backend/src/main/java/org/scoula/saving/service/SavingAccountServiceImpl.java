package org.scoula.saving.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.scoula.common.exception.BusinessException;
import org.scoula.saving.domain.SavingAccountVO;
import org.scoula.saving.dto.SavingAccountCreateRequestDTO;
import org.scoula.saving.mapper.SavingAccountMapper;

@Service
@RequiredArgsConstructor
public class SavingAccountServiceImpl implements SavingAccountService {
    // 신규 등록 시 계좌 상태 기본값 (개설 직후는 항상 정상 납입중)
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final SavingAccountMapper mapper;

    @Override
    @Transactional
    public Long createAccount(
            Long userId, String createdNm, SavingAccountCreateRequestDTO request) {
        this.validateRequest(request);

        SavingAccountVO account = SavingAccountVO.builder()
                .userId(userId)
                .bankCode(request.getBankCode())
                .monthlySave(request.getMonthlySave())
                .monthlyCount(0)
                .currAmount(request.getCurrAmount())
                .accountStatus(STATUS_ACTIVE)
                .build();
        account.setCreatedNm(createdNm);

        this.mapper.insertAccount(account);

        return account.getAccountId();
    }

    // @Valid는 HTTP 요청(Controller) 경로에서만 동작한다. 다른 도메인이 이 메서드를
    // 자바 호출로 직접 부르는 경로까지 방어하기 위해 Service에서도 같은 검증을 한 번 더 한다.
    private void validateRequest(SavingAccountCreateRequestDTO request) {
        if (request.getBankCode() == null || request.getBankCode().isBlank()) {
            throw BusinessException.badRequest("은행코드는 필수입니다.", "SAVE_001");
        }
        if (request.getMonthlySave() == null || request.getMonthlySave() < 0) {
            throw BusinessException.badRequest("월 납입액은 0 이상이어야 합니다.", "SAVE_002");
        }
        if (request.getCurrAmount() == null || request.getCurrAmount() < 0) {
            throw BusinessException.badRequest("누적납입금은 0 이상이어야 합니다.", "SAVE_003");
        }
    }
}

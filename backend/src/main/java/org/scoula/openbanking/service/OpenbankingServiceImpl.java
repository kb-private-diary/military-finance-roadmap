package org.scoula.openbanking.service;

import lombok.RequiredArgsConstructor;

import org.scoula.common.exception.BusinessException;
import org.scoula.openbanking.client.OpenBankingClient;
import org.scoula.openbanking.domain.OpenbankingLinkVO;
import org.scoula.openbanking.dto.OpenbankingLinkRequestDTO;
import org.scoula.openbanking.dto.OpenbankingLinkResponseDTO;
import org.scoula.openbanking.mapper.OpenbankingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpenbankingServiceImpl implements OpenbankingService {

    private final OpenBankingClient client;
    private final OpenbankingMapper mapper;

    @Override
    public String getAuthUrl(Long userId) {
        if (userId == null) {
            throw BusinessException.badRequest("userId 는 필수입니다.", "OPBANK_001");
        }
        return client.buildAuthUrl(userId);
    }

    @Override
    @Transactional
    public OpenbankingLinkResponseDTO linkAccount(Long userId, OpenbankingLinkRequestDTO request) {
        if (userId == null) {
            throw BusinessException.badRequest("userId 는 필수입니다.", "OPBANK_001");
        }
        if (request == null || this.isBlank(request.getAuthCode())) {
            throw BusinessException.badRequest("인증코드(authCode)가 필요합니다.", "OPBANK_002");
        }

        // Mock 클라이언트가 토큰·계좌 정보를 채워 반환
        OpenbankingLinkVO link = client.linkAccount(request.getAuthCode());

        // 이미 연동된 계좌면 중복 방지
        if (mapper.findLinkByFintechUseNum(link.getFintechUseNum()) != null) {
            throw BusinessException.conflict("이미 연동된 계좌입니다.", "OPBANK_003");
        }

        link.setUserId(userId);
        link.setCreatedNm(String.valueOf(userId));
        mapper.insertLink(link);

        return OpenbankingLinkResponseDTO.of(link);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

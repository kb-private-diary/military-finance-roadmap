package org.scoula.openbanking.dto;

import lombok.Builder;
import lombok.Data;

import org.scoula.openbanking.domain.OpenbankingLinkVO;

// 계좌 연동 결과 응답 (민감정보 토큰은 노출하지 않음)
@Data
@Builder
public class OpenbankingLinkResponseDTO {

    private Long linkId;
    private String bankCode;
    private String accountNumMasked;

    public static OpenbankingLinkResponseDTO of(OpenbankingLinkVO vo) {
        return OpenbankingLinkResponseDTO.builder()
                .linkId(vo.getLinkId())
                .bankCode(vo.getBankCode())
                .accountNumMasked(vo.getAccountNumMasked())
                .build();
    }
}

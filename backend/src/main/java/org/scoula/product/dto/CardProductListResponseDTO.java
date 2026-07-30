package org.scoula.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.product.domain.CardProductVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardProductListResponseDTO {
    private Long cardId;
    private String cardName;
    private String cardType;
    private String cardQual;
    private String cardDesc;
    private String cardUrl;

    public static CardProductListResponseDTO of(CardProductVO vo) {
        return CardProductListResponseDTO.builder()
                .cardId(vo.getCardId())
                .cardName(vo.getCardName())
                .cardType(vo.getCardType())
                .cardQual(vo.getCardQual())
                .cardDesc(vo.getCardDesc())
                .cardUrl(vo.getCardUrl())
                .build();
    }
}

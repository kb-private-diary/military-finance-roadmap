package org.scoula.product.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.scoula.common.domain.BaseVO;

// card_product 테이블 매핑 VO (로드맵 추천용 KB 카드 상품 정보)
@Data
@EqualsAndHashCode(callSuper = true)
public class CardProductVO extends BaseVO {
    private Long cardId;
    private Integer category;
    private String cardName;
    private String cardType;
    private String cardQual;
    private String cardDesc;
    private String cardUrl;
}

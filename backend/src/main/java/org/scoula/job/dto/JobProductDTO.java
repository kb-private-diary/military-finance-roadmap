package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.job.domain.JobRecommendServiceVO;
import org.scoula.product.dto.CardProductListResponseDTO;

// 정책·서비스(service_criteria)와 카드 상품(card_product)을 한 목록으로 노출하기 위한 통합 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobProductDTO {

    /** 정책·서비스 구분값 */
    public static final String PRODUCT_TYPE_SERVICE = "SERVICE";

    /** 카드 상품 구분값 */
    public static final String PRODUCT_TYPE_CARD = "CARD";

    private String productType;
    private Long productId;
    private String productName;
    private String productDesc;
    private String badgeCode;
    private String linkUrl;

    public static JobProductDTO ofService(JobRecommendServiceVO vo) {
        return JobProductDTO.builder()
                .productType(PRODUCT_TYPE_SERVICE)
                .productId(vo.getServiceId())
                .productName(vo.getServiceName())
                .productDesc(vo.getServiceDesc())
                .badgeCode(vo.getServiceQual())
                .linkUrl(vo.getServiceUrl())
                .build();
    }

    public static JobProductDTO ofCard(CardProductListResponseDTO dto) {
        return JobProductDTO.builder()
                .productType(PRODUCT_TYPE_CARD)
                .productId(dto.getCardId())
                .productName(dto.getCardName())
                .productDesc(dto.getCardDesc())
                .badgeCode(dto.getCardQual())
                .linkUrl(dto.getCardUrl())
                .build();
    }
}
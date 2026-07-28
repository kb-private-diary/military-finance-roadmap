package org.scoula.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// FSS 상품 동기화(POST /api/products/sync) 실행 결과 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSyncResponseDTO {
    private int savingsCount;
    private int depositsCount;
    private int newProductCount;
    // DB에 없던 신규 상품만 담는다 (is_active=FALSE로 저장되어 수동 검토가 필요한 대상).
    // 기존 상품의 금리 갱신은 일상적인 동작이라 여기 포함하지 않는다.
    private List<ProductSyncItemDTO> newProducts;
}

package org.scoula.travel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 여행 목표의 관심 금융상품 수정 요청 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelProductsUpdateRequestDTO {

    private List<TravelProductSelectionDTO> products;
}

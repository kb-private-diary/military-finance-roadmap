package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.travel.domain.CityCostVO;

/**
 * 도시 물가 응답 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityCostResponseDTO {

    private Long cityCostId;
    private String country;
    private String city;
    private Long savingCost;
    private Long commonCost;
    private Long premiumCost;

    public static CityCostResponseDTO of(CityCostVO vo) {
        return CityCostResponseDTO.builder()
                .cityCostId(vo.getCityCostId())
                .country(vo.getCountry())
                .city(vo.getCity())
                .savingCost(vo.getSavingCost())
                .commonCost(vo.getCommonCost())
                .premiumCost(vo.getPremiumCost())
                .build();
    }
}
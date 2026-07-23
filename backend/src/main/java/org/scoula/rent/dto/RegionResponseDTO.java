package org.scoula.rent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegionResponseDTO {
    private String code;   // 지역 코드 (시군구코드 or 법정동코드)
    private String name;   // 지역 이름 (시도명/시군구명/읍면동명)
}
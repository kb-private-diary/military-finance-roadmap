package org.scoula.rent.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 목표별 희망 지역 등록 요청 (최대 5개 법정동코드)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentGoalRegionCreateRequestDTO {
    private List<String> regionCodes;
}

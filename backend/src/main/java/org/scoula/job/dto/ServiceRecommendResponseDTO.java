package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecommendResponseDTO {
    private Long goalId;

    // 정책(P01)
    private List<JobProductDTO> policies;

    // KB서비스(P02)
    private List<JobProductDTO> financialProducts;
}


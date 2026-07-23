package org.scoula.dashboard.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardBasicResponseDTO {
    // DB 매핑 필드 (조인 결과)
    private String name;
    private String rankName;
    private String typeName;
    private String unitName;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate enlistDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate dischargeDate;

    // 서비스에서 계산될 필드
    private Long totalServiceDays;
    private Long currentServiceDays;
    private Double serviceRate;
}

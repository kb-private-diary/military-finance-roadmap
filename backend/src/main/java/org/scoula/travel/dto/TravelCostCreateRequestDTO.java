package org.scoula.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 예상 경비 산출 요청 DTO
// 도착지·스타일·기간·예산은 저장된 travel_goal 에서 읽으므로
// 요청에는 외부 API 미연동 구간의 항목만 담는다.
// FlightAPI / Booking.com 연동 후 Service 에서 조회한 값으로 대체한다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelCostCreateRequestDTO {

    private Long flightCost;
    private Long hotelCost;
}

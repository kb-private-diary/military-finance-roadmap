package org.scoula.travel.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPackageSearchDTO {

    private String country;
    private String destination;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private Long maxPrice;
}

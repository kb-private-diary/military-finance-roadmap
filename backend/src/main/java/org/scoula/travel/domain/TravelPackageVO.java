package org.scoula.travel.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPackageVO {

    private Long packageId;
    private String goodsCode;
    private String country;
    private String regionName;
    private String name;
    private String imageUrl;
    private String description;
    private Long minPrice;
    private String departurePeriod;
    private LocalDateTime crawledAt;
    private Boolean isActive;
    private String detailUrl;
    private LocalDateTime createdDate;
    private String createdNm;
    private LocalDateTime modifiedDate;
    private String modifiedNm;
    private String delYn;
}

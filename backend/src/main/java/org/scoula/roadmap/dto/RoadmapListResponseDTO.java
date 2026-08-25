package org.scoula.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapListResponseDTO {

    private Long goalId;
    private Long bookmarkId;

    private Integer categoryId;
    private String title;
    private String targetDate;
    private String createdDate;
    private String detail;
    private Boolean bookmarked;

}

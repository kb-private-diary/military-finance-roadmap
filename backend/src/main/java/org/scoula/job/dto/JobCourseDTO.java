package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.JobCourseVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCourseDTO {

    private Long qualId;
    private Long courseId;
    private String courseType;
    private String providerName;
    private String courseName;

    private Long originalPrice;
    private Long discountPrice;
    private Long militaryPrice;

    private Long selectedCost;

    private String benefitDetail;
    private String detailUrl;

    public static JobCourseDTO of(JobCourseVO vo) {
        return JobCourseDTO.builder()
                .qualId(vo.getQualId())
                .courseId(vo.getCourseId())
                .courseType(vo.getCourseType())
                .providerName(vo.getProviderName())
                .courseName(vo.getCourseName())
                .originalPrice(vo.getOriginalPrice())
                .discountPrice(vo.getDiscountPrice())
                .militaryPrice(vo.getMilitaryPrice())
                .selectedCost(vo.getSelectedCost())
                .benefitDetail(vo.getBenefitDetail())
                .detailUrl(vo.getDetailUrl())
                .build();
    }
}

package org.scoula.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.job.domain.PrepItemCriteriaVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrepItemDTO {
    private Long prepCritId;
    private String itemType;
    private String itemName;
    private String infoUrl;
    private String applyUrl;
    private Long amount;
    private String amountSource;
    private String feeDetail;

    public static PrepItemDTO of(PrepItemCriteriaVO vo) {
        return PrepItemDTO.builder()
                .prepCritId(vo.getPrepCritId())
                .itemType(vo.getItemType())
                .itemName(vo.getItemName())
                .infoUrl(vo.getInfoUrl())
                .applyUrl(vo.getApplyUrl())
                .amount(vo.getAmount())
                .amountSource(vo.getAmountSource())
                .feeDetail(vo.getFeeDetail())
                .build();
    }
}

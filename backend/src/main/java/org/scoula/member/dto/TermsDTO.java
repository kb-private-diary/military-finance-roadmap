package org.scoula.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.member.domain.TermsVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermsDTO {
    private Long termsId;
    private String name;
    private Boolean required;
    private String content;
    private String version;

    public static TermsDTO of(TermsVO vo) {
        return new TermsDTO(vo.getTermsId(), vo.getName(), vo.getRequired(), vo.getContent(), vo.getVersion());
    }
}

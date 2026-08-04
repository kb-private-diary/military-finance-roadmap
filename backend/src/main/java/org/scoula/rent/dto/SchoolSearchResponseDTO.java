package org.scoula.rent.dto;

import lombok.Builder;
import lombok.Data;
import org.scoula.rent.domain.SchoolVO;

/**
 * 학교 검색 응답 DTO - 자동완성 목록에 필요한 필드만 담음
 */
@Data
@Builder
public class SchoolSearchResponseDTO {
    private Long schoolId;
    private String schoolName;
    private String address;

    // VO → DTO 변환 (컨벤션: DTO 안에 정적 팩토리 of())
    public static SchoolSearchResponseDTO of(SchoolVO vo) {
        return SchoolSearchResponseDTO.builder()
                .schoolId(vo.getSchoolId())
                .schoolName(vo.getSchoolName())
                .address(vo.getAddress())
                .build();
    }
}

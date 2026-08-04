package org.scoula.openbanking.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.scoula.openbanking.domain.OpenBankingLinkVO;

/**
 * openbanking_link 테이블 접근 Mapper
 * 오픈뱅킹 연동 정보(토큰·계좌)의 저장·조회·해제를 담당
 */
public interface OpenBankingLinkMapper {

    /** 회원 입대일 조회 (신규회원 적금 개설일을 입대일 이후로 보정할 때 사용) */
    LocalDate findEnlistDateByUserId(Long userId);

    /** 연동 1건 저장 (선택된 계좌마다 호출) 생성된 link_id를 VO에 채워줌 */
    int insertLink(OpenBankingLinkVO link);

    /** 회원의 유효한 연동 목록 조회 */
    List<OpenBankingLinkVO> findByUserId(Long userId);

    /** 회원의 유효 연동 건수 (온보딩: 0이면 미연동 → 연동 화면으로) */
    int countByUserId(Long userId);

    /** 핀테크이용번호 중복 연동 여부 (0이면 신규) */
    int countByFintechUseNum(String fintechUseNum);

    /** 회원의 모든 연동 소프트 삭제 (연동 해제·회원 탈퇴 시) */
    int softDeleteByUserId(@Param("userId") Long userId, @Param("actor") String actor);
}

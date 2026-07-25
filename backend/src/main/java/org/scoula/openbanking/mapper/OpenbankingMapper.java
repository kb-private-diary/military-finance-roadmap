package org.scoula.openbanking.mapper;

import org.scoula.openbanking.domain.OpenbankingLinkVO;

public interface OpenbankingMapper {

    // 계좌 연동 정보 저장 (생성된 link_id 반환)
    void insertLink(OpenbankingLinkVO linkVO);

    // 핀테크이용번호로 기존 연동 조회 (중복 연동 방지)
    OpenbankingLinkVO findLinkByFintechUseNum(String fintechUseNum);
}

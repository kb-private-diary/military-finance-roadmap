package org.scoula.push.mapper;

import java.util.List;

import org.scoula.push.domain.PushHistoryVO;

public interface PushHistoryMapper {
    // 발송 결과 기록 (성공/실패 모두)
    void insert(PushHistoryVO vo);

    // 유저의 발송 이력 목록 조회 (최신순)
    List<PushHistoryVO> findListByUserId(Long userId);
}

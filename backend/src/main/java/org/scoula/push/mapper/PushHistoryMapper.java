package org.scoula.push.mapper;

import org.scoula.push.domain.PushHistoryVO;

public interface PushHistoryMapper {
    // 발송 결과 기록 (성공/실패 모두)
    void insert(PushHistoryVO vo);
}

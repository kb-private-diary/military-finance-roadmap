package org.scoula.push.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.scoula.common.domain.BaseVO;

// push_history 테이블 매핑. 발송 시도 1건당 1행(성공/실패 상관없이 기록).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PushHistoryVO extends BaseVO {
    private Long historyId;
    private Long userId;
    private String title;
    private String body;
    private String category; // 프론트 아이콘 매핑용, 값은 호출하는 도메인이 자유롭게 정함
    private String status;   // SUCCESS / FAILED
    private LocalDateTime sentAt;
}

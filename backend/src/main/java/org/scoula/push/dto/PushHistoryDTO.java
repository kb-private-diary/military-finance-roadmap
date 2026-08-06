package org.scoula.push.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.push.domain.PushHistoryVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushHistoryDTO {
    private Long historyId;
    private String title;
    private String body;
    private String status;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime sentAt;

    public static PushHistoryDTO of(PushHistoryVO vo) {
        return new PushHistoryDTO(
                vo.getHistoryId(), vo.getTitle(), vo.getBody(), vo.getStatus(), vo.getSentAt());
    }
}

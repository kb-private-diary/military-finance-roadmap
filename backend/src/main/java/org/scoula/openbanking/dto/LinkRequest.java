package org.scoula.openbanking.dto;

import java.util.List;

import lombok.Data;

/**
 * 오픈뱅킹 계좌 연동 요청
 * 콜백에서 받은 인증정보(code·state)와, 사용자가 목록에서 선택한 계좌들을 담음
 */
@Data
public class LinkRequest {

    private String code;                     // 인증코드 (콜백에서 받은 값)
    private String state;                    // CSRF 방지용 state (요청 시 보낸 값)
    private List<String> selectedFintechNums; // 사용자가 연동 선택한 계좌들의 핀테크이용번호 목록
}

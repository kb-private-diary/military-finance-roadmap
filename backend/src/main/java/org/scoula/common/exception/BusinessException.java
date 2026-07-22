package org.scoula.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직에서 발생하는 예외 (개발 컨벤션 §4).
 *
 * <p>서비스에서 이 예외를 던지기만 하면 {@code ApiExceptionAdvice} 가 받아서
 * 공통 응답 형식({@code ApiResponse})으로 자동 변환한다. try-catch 를 직접 쓸 필요가 없다.
 *
 * <pre>
 * // 조회 실패
 * throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_001");
 *
 * // 잘못된 요청
 * throw BusinessException.badRequest("보증금 한도를 초과했습니다.", "RENT_002");
 *
 * // 권한 없음 (남의 목표 조회 등)
 * throw BusinessException.forbidden("본인의 목표만 조회할 수 있습니다.", "RENT_003");
 * </pre>
 *
 * <p>응답 예시
 * <pre>
 * HTTP 404
 * { "success": false, "data": null, "message": "목표를 찾을 수 없습니다.", "code": "RENT_001", ... }
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 응답 HTTP 상태 코드 */
    private final HttpStatus status;

    /** 도메인 에러 코드 (예: RENT_001). 없으면 null */
    private final String code;

    public BusinessException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    // ------------------------- 자주 쓰는 형태 -------------------------

    /** 400 — 잘못된 요청 (입력값 오류, 규칙 위반 등) */
    public static BusinessException badRequest(String message, String code) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST, code);
    }

    public static BusinessException badRequest(String message) {
        return badRequest(message, null);
    }

    /** 404 — 대상을 찾을 수 없음 */
    public static BusinessException notFound(String message, String code) {
        return new BusinessException(message, HttpStatus.NOT_FOUND, code);
    }

    public static BusinessException notFound(String message) {
        return notFound(message, null);
    }

    /** 403 — 권한 없음 (남의 데이터 접근 등) */
    public static BusinessException forbidden(String message, String code) {
        return new BusinessException(message, HttpStatus.FORBIDDEN, code);
    }

    public static BusinessException forbidden(String message) {
        return forbidden(message, null);
    }

    /** 409 — 중복/충돌 (이미 존재하는 아이디 등) */
    public static BusinessException conflict(String message, String code) {
        return new BusinessException(message, HttpStatus.CONFLICT, code);
    }

    public static BusinessException conflict(String message) {
        return conflict(message, null);
    }
}

package org.scoula.common.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 전 도메인 공통 API 응답 포맷 (개발 컨벤션 §4).
 *
 * <p>컨트롤러는 응답을 이 클래스로 감싸서 반환한다. 프론트엔드는 항상 동일한 형태
 * ({@code success / data / message / code / timestamp})로 받으므로 처리 로직을 통일할 수 있다.
 *
 * <pre>
 * // 조회
 * return ResponseEntity.ok(ApiResponse.success(travelService.findGoals(userId)));
 *
 * // 등록 (생성된 id 반환)
 * return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goalId));
 *
 * // 수정 / 삭제 (본문 없음)
 * return ResponseEntity.ok(ApiResponse.success());
 *
 * // 실패 (예외 처리에서 사용)
 * return ResponseEntity.status(HttpStatus.NOT_FOUND)
 *         .body(ApiResponse.error("목표를 찾을 수 없습니다.", "RENT_001"));
 * </pre>
 *
 * @param <T> 응답 본문 타입
 */
@Getter
public class ApiResponse<T> {

    /** 성공 여부 */
    private final boolean success;

    /** 응답 데이터 (실패 시 null) */
    private final T data;

    /** 안내·에러 메시지 (없으면 null) */
    private final String message;

    /** 에러 코드 (예: RENT_001, 성공 시 null) */
    private final String code;

    /** 응답 생성 시각 (ISO-8601, 예: 2026-07-22T14:30:00) */
    private final String timestamp;

    private ApiResponse(boolean success, T data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // ------------------------- 성공 -------------------------

    /** 데이터만 반환 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /** 데이터 + 안내 메시지 */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    /** 본문 없는 성공 (수정·삭제 등) */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null, null);
    }

    // ------------------------- 실패 -------------------------

    /** 메시지만 */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    /** 메시지 + 에러 코드 (도메인별 코드 규칙: RENT_001, TRAVEL_002 …) */
    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, null, message, code);
    }
}

package org.scoula.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.exception.BusinessException;
import org.scoula.common.response.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * API 공통 예외 처리 (개발 컨벤션 §4).
 *
 * <p>모든 예외를 {@link ApiResponse} 형식으로 변환해 응답한다.
 * 서비스에서는 {@link BusinessException} 만 던지면 되고, 컨트롤러에 try-catch 를 쓸 필요가 없다.
 *
 * <p>※ 404(NoHandlerFoundException)는 SPA 라우팅을 위해
 * {@code CommonExceptionAdvice}(@Order 1)가 먼저 처리한다.
 */
@RestControllerAdvice
@Order(2)
@Log4j2
public class ApiExceptionAdvice {

    /** 비즈니스 예외 — 서비스에서 의도적으로 던진 것 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] {} (code={})", e.getMessage(), e.getCode());
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.error(e.getMessage(), e.getCode()));
    }

    /** 인증 실패 — 잘못된/만료된 refresh token, 존재하지 않는 회원 등 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage(), "AUTH_004"));
    }

    /** 만료/위조된 JWT — refresh token 검증 실패 */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException e) {
        log.warn("[JwtException] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("토큰이 유효하지 않습니다.", "AUTH_004"));
    }

    /** 잘못된 요청 — 파라미터 누락·타입 불일치·본문 파싱 실패 등 */
    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        log.warn("[BadRequest] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("요청 값이 올바르지 않습니다."));
    }

    /** 유효성 검증 실패 — @Valid 사용 시 (첫 번째 오류 메시지를 그대로 전달) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "입력값이 올바르지 않습니다.";
        log.warn("[Validation] {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /** 그 외 예상하지 못한 서버 오류 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Unhandled Exception]", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}

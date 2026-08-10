/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-05
 * 설명: Spring MVC가 Controller 요청 DTO 검증에서 오류 발생 시 처리
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */
package com.sparta.userservice.global.exception.handler;

import com.sparta.userservice.global.exception.CommonErrorCode;
import com.sparta.userservice.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        CommonErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;

        log.warn("AuthenticationException: {}", e.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.fail(
                        e.getMessage(),
                        errorCode.getHttpStatus().value(),
                        errorCode.getErrorKind()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        CommonErrorCode errorCode = CommonErrorCode.FORBIDDEN;

        log.warn("AccessDeniedException: {}", e.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.fail(
                        e.getMessage(),
                        errorCode.getHttpStatus().value(),
                        errorCode.getErrorKind()
                ));
    }
}
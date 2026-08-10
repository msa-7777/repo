/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-05
 * 설명: 서버가 다운된
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

package com.sparta.userservice.global.exception.handler;

import com.sparta.userservice.global.exception.CommonErrorCode;
import com.sparta.userservice.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

        log.error("Unexpected exception: {}", e.getMessage(), e);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.fail(
                        e.getMessage(),
                        errorCode.getHttpStatus().value(),
                        errorCode.getErrorKind()
                ));
    }
}
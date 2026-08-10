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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(" / ")); // 그냥 하나의 message로 만들자

        if (message.isBlank())
            message = errorCode.getMessage();

        // "loginId: 로그인 아이디는 필수입니다. / email: 이메일 형식이 올바르지 않습니다. / password: 비밀번호는 8자 이상이어야 합니다."
        log.warn("ValidationException: {}", message);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.fail(
                        message,
                        errorCode.getHttpStatus().value(),
                        errorCode.getErrorKind()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        CommonErrorCode errorCode = CommonErrorCode.TYPE_MISMATCH;

        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        String message = String.format(
                "parameter=%s, value=%s, requiredType=%s",
                e.getName(), e.getValue(), requiredType
        );

        log.warn("TypeMismatchException: {}", message);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.fail(
                        message,
                        errorCode.getHttpStatus().value(),
                        errorCode.getErrorKind()
                ));
    }
}
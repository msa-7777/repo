/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-05
 * 설명: 개발자가 정의한 BusinessException 처리. log를 찍고, ResponseEntity를 CommonResponse로 return한다.
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

package com.sparta.userservice.global.exception.handler;

import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.exception.ErrorCode;
import com.sparta.userservice.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // log 변수 사용
// @RestController // HTTP 요청을 받아 처리하고, 결과를 주로 JSON으로 반환하는 컨트롤러
@RestControllerAdvice // 모든 @RestController를 지켜보다가 예외가 발생하면, 알맞은 @ExceptionHandler 메서드로 보내주는 역할
public class BusinessExceptionHandler {

    // 비즈니스 오류 (개발자가 지정)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("BusinessException: errorKind={}, message={}", errorCode.getErrorKind(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus()) // HTTP 상태 코드 설정
                .body(CommonResponse.fail(
                        errorCode.getMessage(),
                        errorCode.getHttpStatus().value(),
                        errorCode.getErrorKind()
                )); // 최종 JSON 응답 설정
    }
}
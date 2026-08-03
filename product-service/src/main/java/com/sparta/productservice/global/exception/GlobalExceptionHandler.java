package com.sparta.productservice.global.exception;

import com.sparta.productservice.global.response.RestApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<RestApiResponse<Void>> handleApiException(
            ApiException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        log.warn(
                "API 예외 발생: errorCode={}, status={}, message={}",
                errorCode.getCode(),
                errorCode.getStatus().value(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RestApiResponse.failure(
                        errorCode.getStatus(),
                        errorCode.getMessage(),
                        errorCode.getCode()
                ));
    }


    // Validation 오류 처리
    // 입력값 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(errorCode.getMessage());

        log.warn(
                "요청값 검증 실패: message={}",
                message
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RestApiResponse.failure(
                        errorCode.getStatus(),
                        message,
                        errorCode.getCode()
                ));
    }


    /*
     * 존재하지 않는 정적 리소스나 URL 요청은 서버 장애가 아니므로
     * ERROR 대신 WARN 또는 DEBUG 수준으로 기록한다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException exception
    ) {
        ErrorCode errorCode = CommonErrorCode.RESOURCE_NOT_FOUND;

        log.warn(
                "요청한 리소스를 찾을 수 없습니다: resourcePath={}",
                exception.getResourcePath()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RestApiResponse.failure(
                        errorCode.getStatus(),
                        errorCode.getMessage(),
                        errorCode.getCode()
                ));
    }


    // 위에서 분류되지 않은 진짜 예상 밖 오류만 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestApiResponse<Void>> handleException(
            Exception exception
    ) {
        ErrorCode errorCode =
                CommonErrorCode.INTERNAL_SERVER_ERROR;

        log.error(
                "예상하지 못한 서버 오류가 발생했습니다.",
                exception
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RestApiResponse.failure(
                        errorCode.getStatus(),
                        errorCode.getMessage(),
                        errorCode.getCode()
                ));
    }
}
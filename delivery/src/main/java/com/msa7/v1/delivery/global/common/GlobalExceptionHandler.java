package com.msa7.v1.delivery.global.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 400 - 유효성 검증 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RestApiResponse<Void>> handleValidation(
		MethodArgumentNotValidException e,
		HttpServletRequest request) {

		log.warn("[Validation Failed] path={}, message={}", request.getRequestURI(), e.getMessage());

		String message = e.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.findFirst()
			.orElse("잘못된 입력값입니다.");

		RestApiResponse<Void> response = RestApiResponse.error(
			400,
			message,
			"VALIDATION_ERROR"
		);

		return ResponseEntity.badRequest().body(response);
	}

	// 400 - IllegalArgumentException
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<RestApiResponse<Void>> handleIllegalArgument(
		IllegalArgumentException e,
		HttpServletRequest request) {

		log.warn("[Bad Request] path={}, message={}", request.getRequestURI(), e.getMessage());

		RestApiResponse<Void> response = RestApiResponse.error(
			400,
			e.getMessage(),
			"INVALID_ARGUMENT"
		);

		return ResponseEntity.badRequest().body(response);
	}

	// 커스텀 비즈니스 예외
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<RestApiResponse<Void>> handleBusiness(
		BusinessException e,
		HttpServletRequest request) {

		ErrorCode errorCode = e.getErrorCode();
		int statusCode = errorCode.getStatus().value();

		if (errorCode.getStatus().is4xxClientError()) {
			log.warn("[Business Exception] status={}, path={}, message={}",
				statusCode, request.getRequestURI(), e.getMessage());
		} else {
			log.error("[Business Exception] status={}, path={}, message={}",
				statusCode, request.getRequestURI(), e.getMessage(), e);
		}

		RestApiResponse<Void> response = RestApiResponse.error(
			statusCode,
			e.getMessage(),
			errorCode.name()
		);

		return ResponseEntity.status(errorCode.getStatus()).body(response);
	}

	// 401 - 인증 실패
	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public ResponseEntity<RestApiResponse<Void>> handleAuthentication(
		org.springframework.security.core.AuthenticationException e,
		HttpServletRequest request) {

		log.warn("[Unauthorized] path={}, message={}", request.getRequestURI(), e.getMessage());

		RestApiResponse<Void> response = RestApiResponse.error(
			401,
			"인증에 실패했습니다.",
			"UNAUTHORIZED"
		);

		return ResponseEntity.status(401).body(response);
	}

	// 403 - 권한 없음
	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<RestApiResponse<Void>> handleAccessDenied(
		org.springframework.security.access.AccessDeniedException e,
		HttpServletRequest request) {

		log.warn("[Forbidden] path={}, message={}", request.getRequestURI(), e.getMessage());

		RestApiResponse<Void> response = RestApiResponse.error(
			403,
			"접근 권한이 없습니다.",
			"FORBIDDEN"
		);

		return ResponseEntity.status(403).body(response);
	}

	// 500 - Fallback (반드시 있어야 함)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<RestApiResponse<Void>> handleException(
		Exception e,
		HttpServletRequest request) {

		log.error("[Unhandled Exception] path={}", request.getRequestURI(), e);

		RestApiResponse<Void> response = RestApiResponse.error(
			500,
			"서버 내부 오류가 발생했습니다.",
			"INTERNAL_SERVER_ERROR"
		);

		return ResponseEntity.internalServerError().body(response);
	}
}

package com.msa7.ai.global.common;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 400
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력값입니다."),

	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증에 실패했습니다."),

	// 403
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

	// 404
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다."),

	// 409
	CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "리소스 충돌이 발생했습니다."),

	// 500
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE",  "잘못된 입력값입니다."),
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND", "해당 업체를 찾을 수 없습니다."),
	DUPLICATE_COMPANY_NAME(HttpStatus.BAD_REQUEST, "DUPLICATE_COMPANY_NAME", "업체의 이름이 중복되었습니다."),
	HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "HUB_NOT_FOUND", "해당 관리 허브를 찾을 수 없거나 유효하지 않습니다.");

    private final HttpStatus status;
	private final String code;
	private final String message;
}

package com.msa7.company.global.common;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 400
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),

	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

	// 403
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

	// 404
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

	// 409
	CONFLICT(HttpStatus.CONFLICT, "리소스 충돌이 발생했습니다."),

	// 500
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,  "잘못된 입력값입니다."),
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 업체를 찾을 수 없습니다."),
	HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 관리 허브를 찾을 수 없거나 유효하지 않습니다.");

    private final HttpStatus status;
	private final String message;
}

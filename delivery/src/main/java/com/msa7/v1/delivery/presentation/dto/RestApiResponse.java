package com.msa7.v1.delivery.presentation.dto;

public record RestApiResponse<T>(
	// Swagger의 @ApiResponse와 충돌 가능성 사전 차단
	boolean success,   // 요청 성공 여부
	int code,          // HTTP 상태 코드와 동일한 값
	String message,    // 사람이 읽는 결과 설명 (한국어)
	T data,            // 성공 시 페이로드, 실패 시 null
	String error       // 실패 시 에러 식별 코드(대문자 스네이크), 성공 시 null
) {

	// 성공 응답
	public static <T> RestApiResponse<T> ok(T data) {
		return new RestApiResponse<>(true, 200, "요청이 성공했습니다.", data, null);
	}

	public static <T> RestApiResponse<T> ok(String message, T data) {
		return new RestApiResponse<>(true, 200, message, data, null);
	}

	// 에러 응답
	public static <T> RestApiResponse<T> error(int code, String message, String errorCode) {
		return new RestApiResponse<>(false, code, message, null, errorCode);
	}

	public static <T> RestApiResponse<T> error(int code, String message) {
		return new RestApiResponse<>(false, code, message, null, null);
	}


}
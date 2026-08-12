/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: RestAPI 공통 응답 포맷 통일

 * payload = 응답에서 실제로 전달하려는 핵심 데이터
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
package com.msa7.company.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON 변환 시 null인 필드 제거
public class CommonResponse<T> {

    private final boolean success;  // 요청 성공 여부                                         [ex) true, false]
    private final String message;   // 사람이 읽는 결과 설명 (한국어)                           [ex) 이미 사용 중인 닉네임입니다, null]
    private final T data;           // 성공 시 페이로드, 실패 시 null                          [ex) data의 페이로드]

    private final int code;         // HTTP 상태 코드와 동일한 값                              [ex) 2xx / 4xx / 5xx / ...]
    private final String errorKind;     // 실패 시 에러 식별 코드(대문자 스네이크), 성공 시 null   [ex) NICKNAME_DUPLICATE, null]

    private static final int DEFAULT_SUCCESS_CODE = 200;
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";
    private static final int DEFAULT_FAIL_CODE = 999;
    private static final String DEFAULT_FAIL_MESSAGE = "요청에 실패했습니다.";
    private static final String DEFAULT_FAIL_ERROR = "INTERNAL_SERVER_ERROR";

    private CommonResponse(boolean success, String message, T data, int code, String errorKind) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
        this.errorKind = errorKind;
    }


    public static <T> CommonResponse<T> success() {
        return success(DEFAULT_SUCCESS_MESSAGE, null, DEFAULT_SUCCESS_CODE);
    }
    public static <T> CommonResponse<T> success(T data) {
        return success(DEFAULT_SUCCESS_MESSAGE, data, DEFAULT_SUCCESS_CODE);
    }
    public static <T> CommonResponse<T> success(String message, T data) {
        return success(message, data, DEFAULT_SUCCESS_CODE);
    }
    public static <T> CommonResponse<T> success(String message, T data, int code) {
        return new CommonResponse<>(true, message, data, code, null); // 성공 시 error message는 null
    }


    public static <T> CommonResponse<T> fail() {
        return fail(DEFAULT_FAIL_MESSAGE, DEFAULT_FAIL_CODE, DEFAULT_FAIL_ERROR);
    }
    public static <T> CommonResponse<T> fail(String message) {
        return fail(message, DEFAULT_FAIL_CODE, DEFAULT_FAIL_ERROR);
    }
    public static <T> CommonResponse<T> fail(String message, int code, String errorKind) {
        return new CommonResponse<>(false, message, null, code, errorKind); // 실패 시 data는 null
    }
}

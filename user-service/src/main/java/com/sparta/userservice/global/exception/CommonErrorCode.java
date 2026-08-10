/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: 애플리케이션에서 공통 / 기능별 사용하는 에러코드와 HTTP 상태, 메시지 정의하는 enum (ErrorCode를 상속받아야 함)
 * 정책: errorKind는 enum 상수 이름과 동일하기에, String errorKind 필드를 만들지 않고, enum.name()으로 대체
 * 특정 도메인에서 ErrorCode enum 작성 방법
 NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
 ALREADY_DELETED_LAST_ADDRESS(HttpStatus.CONFLICT, "기본 배송지 하나만 있는 경우 삭제 처리가 불가합니다."),

 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

package com.sparta.userservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 파라미터 형식이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
    // private final String errorKind;

    @Override
    public String getErrorKind() {
        return name(); // enum 상수 이름을 반환
    }
}

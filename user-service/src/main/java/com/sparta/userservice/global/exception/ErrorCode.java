/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: Error 발생 시 HTTP 상태 코드와 에러 메시지를 제공하도록 interface 정의
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

package com.sparta.userservice.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getHttpStatus();
    String getMessage();
    String getErrorKind();
}

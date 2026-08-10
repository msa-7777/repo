/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: 서비스 로직에서 발생한 비지니스 오류를 공통 형식으로 던지기 위한 사용자 정의 예외 클래스
 * 사용 예시
 throw new BusinessException(
    GlobalErrorCode.INVALID_INPUT_VALUE
 );
 public User findUser(UUID userId) {
    return userRepository.findById(userId)
            .orElseThrow(() ->
                new BusinessException(UserErrorCode.USER_NOT_FOUND)
            );
 }
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

package com.sparta.userservice.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 자바 예외 시스템과 로깅에서 사용할 표준 예외 메시지를 저장
        this.errorCode = errorCode;
    }
}

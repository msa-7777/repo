package com.sparta.userservice.domain.exception;

import com.sparta.userservice.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
    // 회원 정보 중복
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    // DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 이름입니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    // DUPLICATE_SLACK_ID(HttpStatus.CONFLICT, "이미 사용 중인 Slack ID입니다."),

    // 사용자 조회 및 로그인
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    DELETED_USER(HttpStatus.UNAUTHORIZED, "탈퇴한 사용자입니다."),

    // 회원 정보 수정
    UPDATE_FAILURE(HttpStatus.CONFLICT, "회원 정보를 수정할 수 없습니다."),

    // 회원 삭제
    INVALID_DELETE_CREDENTIALS(HttpStatus.UNAUTHORIZED, "회원 탈퇴를 위한 인증 정보가 일치하지 않습니다."),
    ALREADY_DELETED_USER(HttpStatus.CONFLICT, "이미 탈퇴 처리된 사용자입니다."),
    DELETE_FAILURE_LAST_MASTER(HttpStatus.CONFLICT, "현재 사용자가 마지막 관리자이므로 탈퇴할 수 없습니다. 다른 관리자를 지정한 후 다시 시도해 주세요."),

    // 회원가입 요청
    NOT_FOUND_SIGNUP_REQUEST(HttpStatus.NOT_FOUND, "존재하지 않는 회원가입 요청입니다."),
    SIGNUP_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 회원가입 요청입니다."),
    SIGNUP_REQUEST_NOT_PENDING(HttpStatus.CONFLICT, "승인 대기 상태인 회원가입 요청만 처리할 수 있습니다."),
    SIGNUP_REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "회원가입 요청 거절 사유를 입력해 주세요."),

    DIFFERENT_HUB_ACCESS_DENIED(HttpStatus.FORBIDDEN, "동일한 허브에 소속된 회원의 가입 요청만 승인하거나 거절할 수 있습니다."),

    // 소속 정보
    HUB_REQUIRED(HttpStatus.BAD_REQUEST, "허브 관리자 또는 배송 담당자는 소속 허브가 필요합니다."),
    SUPPLIER_REQUIRED(HttpStatus.BAD_REQUEST, "생산 업체 담당자는 소속 업체가 필요합니다."),
    INVALID_HUB(HttpStatus.NOT_FOUND, "존재하지 않는 허브입니다."),
    INVALID_SUPPLIER(HttpStatus.NOT_FOUND, "존재하지 않는 생산 업체입니다."),


    // 배송 담당자를 찾을 수 없는 경우
    NOT_FOUND_DELIVERY_MANAGER(HttpStatus.NOT_FOUND,"배송 담당자를 찾을 수 없습니다.")



    ;

    private final HttpStatus httpStatus;
    private final String message;
    // private final String errorKind;

    UserErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
    @Override
    public HttpStatus getHttpStatus() { return httpStatus; }
    @Override
    public String getMessage() { return message; }
    @Override
    public String getErrorKind() { return name(); }
}

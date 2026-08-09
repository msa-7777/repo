package com.msa7.hub.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    CENTRAL_HUB_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 중앙 허브입니다."),
    SAME_HUB_ROUTE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "출발 허브 ID와 도착 허브 ID가 같은 값입니다."),
    INVALID_HUB_ROUTE(HttpStatus.BAD_REQUEST, "유효하지 않은 허브 경로입니다. 중앙허브와 소속 허브 간, 또는 중앙허브 간 경로만 등록할 수 있습니다."),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 404
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브를 찾을 수 없습니다."),
    HUB_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 허브 라우트 입니다."),

    // 409
    CONFLICT(HttpStatus.CONFLICT, "리소스 충돌이 발생했습니다."),
    HUB_ROUTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 허브 경로입니다."),
    HUB_REFERENCED_BY_COMPANY(HttpStatus.CONFLICT, "해당 허브를 참조하는 업체가 존재하여 삭제할 수 없습니다."),
    HUB_REFERENCED_BY_INVENTORY(HttpStatus.CONFLICT, "해당 허브에 재고가 존재하여 삭제할 수 없습니다."),
    HUB_REFERENCED_BY_CHILD_HUB(HttpStatus.CONFLICT, "해당 허브를 중앙 허브로 참조하는 소속 허브가 존재하여 삭제할 수 없습니다."),
    HUB_REFERENCED_BY_HUB_ROUTE(HttpStatus.CONFLICT, "해당 허브를 사용하는 허브 경로가 존재하여 삭제할 수 없습니다."),
    HUB_REFERENCED_BY_USER(HttpStatus.CONFLICT, "해당 허브를 참조하는 유저가 존재하여 삭제할 수 없습니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 503
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "외부 서비스 상태를 확인할 수 없습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}

package com.sparta.slackservice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlackFailureReason {
    // DB 저장용

    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    SLACK_ID_NOT_FOUND("Slack ID가 존재하지 않습니다."),
    CHANNEL_CREATE_FAILED("DM 채널 생성에 실패했습니다."),
    MESSAGE_SEND_FAILED("Slack 메시지 발송에 실패했습니다."),
    MESSAGE_UPDATE_FAILED("Slack 메시지 수정에 실패했습니다."),
    API_TIMEOUT("Slack API 응답 시간이 초과되었습니다.");

    private final String message;
}

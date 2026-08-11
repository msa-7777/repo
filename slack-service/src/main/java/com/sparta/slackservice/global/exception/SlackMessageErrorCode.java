package com.sparta.slackservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SlackMessageErrorCode implements ErrorCode {
    // API 응답용

    SLACK_MESSAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SLACK_MESSAGE_NOT_FOUND",
            "Slack 메시지를 찾을 수 없습니다."
    ),

    SLACK_MESSAGE_RECEIVER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SLACK_MESSAGE_RECEIVER_NOT_FOUND",
            "Slack 메시지 수신자 정보를 찾을 수 없습니다."
    ),

    SLACK_MESSAGE_RECEIVER_SLACK_ID_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "SLACK_MESSAGE_RECEIVER_SLACK_ID_NOT_FOUND",
            "Slack 메시지 수신자의 Slack ID가 존재하지 않습니다."
    ),

    SLACK_MESSAGE_NOT_MODIFIABLE(
            HttpStatus.BAD_REQUEST,
            "SLACK_MESSAGE_NOT_MODIFIABLE",
            "발송에 성공한 Slack 메시지만 수정할 수 있습니다."
    ),

    SLACK_MESSAGE_IDENTIFIER_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "SLACK_MESSAGE_IDENTIFIER_NOT_FOUND",
            "Slack 메시지 식별 정보가 존재하지 않습니다."
    ),

    SLACK_MESSAGE_SEND_FAILED(
            HttpStatus.BAD_GATEWAY,
            "SLACK_MESSAGE_SEND_FAILED",
            "Slack 메시지 발송에 실패했습니다."
    ),

    SLACK_MESSAGE_UPDATE_FAILED(
            HttpStatus.BAD_GATEWAY,
            "SLACK_MESSAGE_UPDATE_FAILED",
            "Slack 메시지 수정에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}


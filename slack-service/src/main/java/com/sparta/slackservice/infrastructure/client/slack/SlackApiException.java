package com.sparta.slackservice.infrastructure.client.slack;

import lombok.Getter;

@Getter
public class SlackApiException extends RuntimeException {
    // Slack API 전용 예외

    private final String slackError;

    public SlackApiException(String message, String slackError) {
        super(message);
        this.slackError = slackError;
    }

    public SlackApiException(String message, String slackError, Throwable cause) {
        super(message, cause);
        this.slackError = slackError;
    }
}
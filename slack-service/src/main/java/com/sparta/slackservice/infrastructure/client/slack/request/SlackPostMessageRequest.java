package com.sparta.slackservice.infrastructure.client.slack.request;

public record SlackPostMessageRequest(
        // Slack API 발송 요청 DTO
        String channel,     // Slack 사용자 ID인 slackUserId
        String text
) {

}

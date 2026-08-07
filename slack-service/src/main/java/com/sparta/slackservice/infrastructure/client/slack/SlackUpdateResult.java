package com.sparta.slackservice.infrastructure.client.slack;

public record SlackUpdateResult(
        // 서비스로 반환할 결과 DTO
        // SlackMessageService에 전달할 내부 결과 구조

        String channelId,
        String slackTs
) {
}
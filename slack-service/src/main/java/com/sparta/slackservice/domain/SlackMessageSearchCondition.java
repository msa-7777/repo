package com.sparta.slackservice.domain;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackMessageSearchCondition(
        // 검색 조건

        UUID orderId,

        UUID hubId,

        UUID receiverId,

        String receiverName,

        SlackMessageStatus status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate
) {
}

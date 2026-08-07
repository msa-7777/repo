package com.sparta.slackservice.infrastructure.client.user;

import java.util.UUID;

public record UserResponse(

        UUID userId,
        String name,
        String slackId
) {
}
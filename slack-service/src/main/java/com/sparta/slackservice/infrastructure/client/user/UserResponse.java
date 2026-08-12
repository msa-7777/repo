package com.sparta.slackservice.infrastructure.client.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(

        UUID userId,
        String name,
        String slackId
) {
}
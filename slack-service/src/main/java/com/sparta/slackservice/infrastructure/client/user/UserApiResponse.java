package com.sparta.slackservice.infrastructure.client.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserApiResponse<T>(
        boolean success,
        String message,
        T data,
        int code,
        String errorKind
) {
}
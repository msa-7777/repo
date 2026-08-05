package com.sparta.userservice.global.response;

import org.springframework.http.HttpStatus;

public record RestApiResponse<T>(
        boolean success,
        int code,
        String message,
        T data,
        String error
) {

    public static <T> RestApiResponse<T> success(
            HttpStatus status,
            String message,
            T data
    ) {
        return new RestApiResponse<>(
                true,
                status.value(),
                message,
                data,
                null
        );
    }

    public static RestApiResponse<Void> failure(
            HttpStatus status,
            String message,
            String error
    ) {
        return new RestApiResponse<>(
                false,
                status.value(),
                message,
                null,
                error
        );
    }
}
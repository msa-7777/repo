package com.sparta.slackservice.infrastructure.client.user;

import com.sparta.slackservice.global.response.RestApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    // Slack 수신자 정보를 조회하기 위한 user-service 내부 API
    @GetMapping("/api/v1/internal/users/{userId}")
    UserApiResponse<UserResponse> getUser(
            @PathVariable("userId") UUID userId
    );
}
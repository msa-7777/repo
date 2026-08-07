package com.sparta.slackservice.infrastructure.client.user;

import com.sparta.slackservice.global.response.RestApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    // TODO: user-service 내부 Slack 정보 조회 API 경로와 응답 형식 확정 후 수정
    // 사용자관리 API 중에 개별 회원 조회 API 재활용
    @GetMapping("/api/v1/admin/users/{userId}")
    RestApiResponse<UserResponse> getUser(
            @PathVariable("userId") UUID userId
    );
}
package com.msa7.hub.infrastructure.client;

import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.infrastructure.client.dto.PageResponse;
import com.msa7.hub.infrastructure.client.dto.UserResponse;
import com.msa7.hub.infrastructure.client.fallback.UserClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    @GetMapping("/api/v1/users")
    RestApiResponse<PageResponse<UserResponse>> getUserList(@RequestParam UUID hubId);
}

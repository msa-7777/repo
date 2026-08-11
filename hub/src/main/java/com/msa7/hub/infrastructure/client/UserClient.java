package com.msa7.hub.infrastructure.client;

import com.msa7.hub.infrastructure.client.fallback.UserClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    @GetMapping("/api/v1/internal/users/exists")
    boolean existsUserByHubId(@RequestParam UUID hubId);
}

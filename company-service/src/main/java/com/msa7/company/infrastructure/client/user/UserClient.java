package com.msa7.company.infrastructure.client.user;

import com.msa7.company.global.response.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// #TODO : user조회 내부 API 만들어지면 수정해야 함
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/v1/internal/users/{userId}")
    ResponseEntity<CommonResponse<UserResponse>> getUserById(@PathVariable("userId") UUID userId);

}

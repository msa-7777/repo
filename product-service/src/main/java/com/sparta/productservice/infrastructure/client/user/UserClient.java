package com.sparta.productservice.infrastructure.client.user;

import com.sparta.productservice.global.response.RestApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    // 상품의 리소스 접근 권한 검증에 필요한 사용자 정보를 조회한다.
    // SUPPLIER_AGENT의 supplierId와 HUB_MANAGER의 hubId를 확인하기 위해 사용한다.
    @GetMapping("/api/v1/internal/users/{userId}")
    RestApiResponse<UserResponse> getUser(
            @PathVariable("userId") UUID userId
    );

}

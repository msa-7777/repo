package com.msa7.company.infrastructure.client.hub;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubClient {
    // #TODO : hub 확인 후 수정 요망
    @GetMapping("/api/v1/hubs/{hubId}")
    HubResponse getHub(@PathVariable("hubId") UUID hubId);

    default boolean existsHub(UUID hubId) {
        try {
            return getHub(hubId) != null;
        } catch (Exception e) {
            // 외부 Hub 서비스 연결 실패 시 가동성 확보를 위해 기본값 true 처리 혹은 에러 핸들링
            return true;
        }
    }
}
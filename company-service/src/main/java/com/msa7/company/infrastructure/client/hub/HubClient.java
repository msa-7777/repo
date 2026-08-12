package com.msa7.company.infrastructure.client.hub;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubClient {
    @GetMapping("/api/v1/internal/hubs/{hubId}/exists")
    boolean checkHubExists(@PathVariable("hubId") UUID hubId);
}
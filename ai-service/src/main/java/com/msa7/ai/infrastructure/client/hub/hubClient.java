package com.msa7.ai.infrastructure.client.hub;

import com.msa7.ai.infrastructure.client.delivery.DeliveryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface hubClient {

    @GetMapping("/api/v1/internal/hubs/{startHubId}/routes/{endHubId}") // 혹은 id 기준
    HubRoutePathResponse getRouteInfo(@PathVariable("startHubId") UUID startHubId,
                                      @PathVariable("endHubId") UUID endHubId);
}
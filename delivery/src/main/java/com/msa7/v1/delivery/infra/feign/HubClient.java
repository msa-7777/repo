package com.msa7.v1.delivery.infra.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.msa7.v1.delivery.presentation.dto.HubRouteResponse;

@FeignClient(name = "hub-service", path = "/api/v1/hubs")
public interface HubClient {
	@GetMapping("/{startHubId}/routes/{endHubId}")
	HubRouteResponse getRouteInfo(@PathVariable UUID startHubId, @PathVariable UUID endHubId);
}

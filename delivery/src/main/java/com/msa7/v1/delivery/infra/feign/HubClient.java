package com.msa7.v1.delivery.infra.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.msa7.v1.delivery.presentation.dto.HubRouteResponse;
import com.msa7.v1.delivery.presentation.dto.RestApiResponse;
import com.msa7.v1.delivery.presentation.dto.payload.HubResponse;
import com.msa7.v1.delivery.presentation.dto.payload.HubRoutePathRequest;
import com.msa7.v1.delivery.presentation.dto.payload.HubRoutePathResponse;

@FeignClient(name = "hub-service", path = "/api/v1/hubs")
public interface HubClient {
	// // 특정 허브 존재 여부 확인
	// @GetMapping("/api/v1/hubs/{hubId}")
	// RestApiResponse<HubResponse> getHub(@PathVariable("hubId") UUID hubId);
	//
	// // 출발 허브에서 목적지 허브까지의 모든 중간 경로 정보를 리스트로 반환
	// @GetMapping("/api/v1/hub-routes/path")
	// RestApiResponse<HubRoutePathResponse> getHubRoutePath(
	// 	@SpringQueryMap HubRoutePathRequest request
	// );
	// 특정 허브 존재 여부 확인
	@GetMapping("/{hubId}/exists")
	boolean checkHubExists(@PathVariable UUID hubId);

	// 출발 허브에서 목적지 허브까지의 모든 중간 경로 정보를 리스트로 반환
	@GetMapping("/{startHubId}/routes/{endHubId}")
	HubRouteResponse getRouteInfo(@PathVariable UUID startHubId, @PathVariable UUID endHubId);
}

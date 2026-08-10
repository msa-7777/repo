package com.msa7.v1.order.app.feign;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.msa7.v1.order.presentation.dto.onlycontoller.RestApiResponse;
import com.msa7.v1.order.presentation.dto.payload.DeliveryResponse;
import com.msa7.v1.order.presentation.dto.payload.DeliveryRouteResponse;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {
	@GetMapping("/api/v1/deliveries/{deliveryId}")
	RestApiResponse<DeliveryResponse> getDeliveryInfo(@PathVariable("deliveryId") UUID deliveryId);

	@GetMapping("/api/v1/deliveries/routes")
	RestApiResponse<List<DeliveryRouteResponse>> getDeliveryRoutes(@RequestParam("hubId") UUID hubId, @RequestParam("status") String status);
}

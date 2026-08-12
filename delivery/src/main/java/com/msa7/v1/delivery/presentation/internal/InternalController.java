package com.msa7.v1.delivery.presentation.internal;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msa7.v1.delivery.app.DeliveryService;
import com.msa7.v1.delivery.presentation.internal.dto.DeliveryRouteInfoResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/internal/deliveries")
@RequiredArgsConstructor
public class InternalController {
	private final DeliveryService deliveryService;

	@GetMapping("/exists")
	public boolean existsActiveDeliveryByHubId(@RequestParam UUID hubId) {
		return deliveryService.existsActiveDeliveryByHubId(hubId);
	}

	@GetMapping("/{orderId}/info")
	public DeliveryRouteInfoResponse getDeliveryRouteInfo(@PathVariable UUID orderId) {
		return deliveryService.getDeliveryInfoByOrderId(orderId);
	}
}

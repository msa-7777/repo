package com.msa7.v1.order.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.msa7.v1.order.infra.feign.dto.CreateDeliveryRequest;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {
	@PostMapping("/api/v1/deliveries")
	void createDelivery(@RequestBody CreateDeliveryRequest request);
}

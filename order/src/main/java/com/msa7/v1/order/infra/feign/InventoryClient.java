package com.msa7.v1.order.infra.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface InventoryClient {
	@GetMapping("/api/v1/inventories/{productId}")
	void verifyInventory(@PathVariable("productId") UUID productId);

}

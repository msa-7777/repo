package com.msa7.v1.order.infra.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface InventoryClient {
	@GetMapping("/internal/inventories/{productId}")
	void verifyInventory(@PathVariable("productId") UUID productId, @RequestParam("quantity") Integer quantity);

	// @PostMapping("/api/v1/products/{productId}/verify-stock")
	// void verifyProductAndStock(@PathVariable("productId") UUID productId, @RequestParam("quantity") Integer quantity);
}

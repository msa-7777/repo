package com.msa7.hub.infrastructure.client;

import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.infrastructure.client.dto.InventoryResponse;
import com.msa7.hub.infrastructure.client.dto.PageResponse;
import com.msa7.hub.infrastructure.client.fallback.InventoryClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product-service", fallbackFactory = InventoryClientFallbackFactory.class)
public interface InventoryClient {

    @GetMapping("/api/v1/inventories")
    RestApiResponse<PageResponse<InventoryResponse>> getInventoryList(@RequestParam UUID hubId);
}

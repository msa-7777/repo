package com.msa7.hub.infrastructure.client;

import com.msa7.hub.infrastructure.client.fallback.DeliveryFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "delivery-service", fallbackFactory = DeliveryFallbackFactory.class)
public interface DeliveryClient {

    @GetMapping("/api/v1/internal/deliveries/exists")
    boolean existsActiveDeliveryByHubId(@RequestParam UUID hubId);
}
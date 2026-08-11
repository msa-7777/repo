package com.msa7.ai.infrastructure.client.delivery;

import com.msa7.ai.infrastructure.client.delivery.DeliveryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {

    @GetMapping("/api/v1/deliveries/order/{orderId}") // 혹은 id 기준
    DeliveryResponse getDeliveryByOrder(@PathVariable("orderId") UUID orderId);


}
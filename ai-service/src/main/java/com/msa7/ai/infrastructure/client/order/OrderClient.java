package com.msa7.ai.infrastructure.client.order;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service") // Eureka에 등록된 서비스명 혹은 게이트웨이 경로
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    OrderResponse getOrder(@PathVariable("orderId") UUID orderId);

    //orderId로 delivery-service orderId/orderStatus/deliveryStatus/deliveryId 조회
    @GetMapping("/api/v1/orders/{orderId}/delivery-status")
    OrderWithDeliveryDto getOrderWithDeliveryStatus(@PathVariable("orderId") UUID orderId);


}
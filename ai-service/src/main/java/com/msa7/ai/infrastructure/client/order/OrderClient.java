package com.msa7.ai.infrastructure.client.order;


import com.msa7.ai.global.response.RestApiResponse;
import com.msa7.ai.infrastructure.client.order.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service") // Eureka에 등록된 서비스명 혹은 게이트웨이 경로
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    OrderResponse getOrder(@PathVariable("orderId") UUID orderId);

    @GetMapping("/api/v1/internal/orders/{orderId}/status-detail")
    ResponseEntity<OrderResponse> getOrderDetail(@PathVariable("orderId") UUID orderId);

    @GetMapping("/api/v1/orders/{orderId}/delivery-status")
    ResponseEntity<OrderWithDeliveryDto> getOrderWithDeliveryStatus(@PathVariable("orderId") UUID orderId);
}
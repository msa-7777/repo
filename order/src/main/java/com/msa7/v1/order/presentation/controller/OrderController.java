package com.msa7.v1.order.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa7.v1.order.app.OrderService;
import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.presentation.dto.payload.CreateOrderRequest;
import com.msa7.v1.order.presentation.dto.payload.OrderResponse;
import com.msa7.v1.order.presentation.dto.onlycontoller.RestApiResponse;
import com.msa7.v1.order.presentation.dto.payload.OrderWithDeliveryDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// 주문 생성
	@PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'DELIVERY_AGENT', 'SUPPLIER_AGENT')")
	@PostMapping
	public ResponseEntity<RestApiResponse<OrderResponse>> createOrder(@RequestBody CreateOrderRequest request) {
		Order order = orderService.createOrder(request.receiverCompanyId(),
			request.productId(), request.quantity(), request.requestNotes(),
			request.receiverSlackId(), request.startHubId(), request.endHubId(),
			request.destinationAddress());
		return ResponseEntity.ok(RestApiResponse.ok(OrderResponse.from(order)));
	}

	@PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'DELIVERY_AGENT', 'SUPPLIER_AGENT')")
	@GetMapping("/{orderId}")
	public ResponseEntity<RestApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
		Order order = orderService.getOrder(orderId);
		return ResponseEntity.ok(RestApiResponse.ok(OrderResponse.from(order)));
	}

	@PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'DELIVERY_AGENT', 'SUPPLIER_AGENT')")
	@GetMapping("/{orderId}/delivery-status")
	public ResponseEntity<RestApiResponse<OrderWithDeliveryDto>>
	getOrderWithDeliveryStatus(@PathVariable UUID orderId) {
		OrderWithDeliveryDto response = orderService.getOrderWithDeliveryStatus(orderId);
		return ResponseEntity.ok(RestApiResponse.ok(response));
	}
}

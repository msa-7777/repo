package com.msa7.v1.order.presentation.internal;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa7.v1.order.app.OrderService;
import com.msa7.v1.order.domain.aggregate.Order;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
public class InternalController {
	private final OrderService orderService;

	@GetMapping("{orderId}")
	public ResponseEntity<InternalOrderResponse> getOrderInfo(@PathVariable UUID orderId) {
		Order order = orderService.getOrder(orderId);
		InternalOrderResponse response = InternalOrderResponse.builder()
			.orderId(order.getId())
			.receiverCompanyId(order.getReceiverCompanyId())
			.status(order.getStatus())
			.build();
		return ResponseEntity.ok(response);
	}
}

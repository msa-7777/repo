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

	@GetMapping("/{orderId}/status-detail")
	public ResponseEntity<InternalOrderDetailResponse> getOrderDetail(@PathVariable UUID orderId) {
		Order order = orderService.getOrder(orderId);
		InternalOrderDetailResponse response = new InternalOrderDetailResponse(
			order.getId(),
			order.getProductId(),
			order.getQuantity().value(),
			order.getRequestNotes().contents(),
			order.getStatus(),
			order.getReceiverCompanyId()
		);

		return ResponseEntity.ok(response);
	}
}

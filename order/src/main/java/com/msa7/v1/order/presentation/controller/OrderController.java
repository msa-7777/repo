package com.msa7.v1.order.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa7.v1.order.app.OrderService;
import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.presentation.dto.onlycontoller.CreateOrderRequest;
import com.msa7.v1.order.presentation.dto.onlycontoller.OrderResponse;
import com.msa7.v1.order.presentation.dto.onlycontoller.RestApiResponse;
import com.msa7.v1.order.presentation.dto.onlycontoller.UpdateOrderRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// 주문 생성
	@PostMapping
	public ResponseEntity<RestApiResponse<OrderResponse>>
	// @RequestHeader("X-Request-ID") UUID requestId 추가 -> apiGW에서 넘어온 유저 식별자
	// @RequestHeader("X-Role") String role 추가
	createOrder(
		@RequestBody CreateOrderRequest request) {
		Order order = orderService.createOrder(
			// requestId,
			request.receiverCompanyId(),
			request.productId(),
			request.quantity(),
			request.requestNotes()
		);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(new RestApiResponse<>(true, 201, "주문이 생성되었습니다.", OrderResponse.from(order), null));
	}

	// 주문 단건 조회
	@GetMapping("/{orderId}")
	public ResponseEntity<RestApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
		Order order = orderService.getOrder(orderId);
		return ResponseEntity.ok(RestApiResponse.ok(OrderResponse.from(order)));
	}


	// 주문 수정
	@PutMapping("/{orderId}")
	public ResponseEntity<RestApiResponse<OrderResponse>> updateOrder(
		@PathVariable UUID orderId,
		@RequestBody UpdateOrderRequest request) {
		Order order = orderService.updateOrder(
			orderId,
			request.quantity(),
			request.requestNotes()
		);
		return ResponseEntity.ok(RestApiResponse.ok("주문이 수정되었습니다.", OrderResponse.from(order)));
	}

	// 주문 취소
	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<RestApiResponse<Void>> cancelOrder(@PathVariable UUID orderId) {
		orderService.cancelOrder(orderId);
		return ResponseEntity.ok(RestApiResponse.ok("주문이 취소되었습니다.", null));
	}

	// 주문 삭제
	@DeleteMapping("/{orderId}")
	public ResponseEntity<RestApiResponse<Void>>
	deleteOrder(@PathVariable UUID orderId)
	// @RequestHeader("X-User-Id") UUID userId
	{
		orderService.deleteOrder(orderId);
		return ResponseEntity.ok(RestApiResponse.ok("주문이 삭제되었습니다.", null));
	}

}

package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

import com.msa7.v1.order.domain.aggregate.Order;

public record OrderResponse(
	UUID orderId,
	UUID receiverId,
	UUID productId,
	Integer quantity
) {
	// 도메인 객체 -> DTO 변환 팩토리 메서드
	public static OrderResponse from(Order order) {
		return new OrderResponse(
			order.getId(),
			order.getReceiverCompanyId(),
			order.getProductId(),
			order.getQuantity().value()
		);
	}
}

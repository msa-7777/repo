package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

import com.msa7.v1.order.domain.aggregate.Order;

// order -> delivery 배송 생성 요청
public record OrderCreatedEvent(
	UUID orderId,
	UUID receiverId,
	UUID receiverSlackId,
	UUID startHubId,
	UUID endHubId,
	String destinationAddress
) {
	public static OrderCreatedEvent from(Order order, UUID receiverId
	, UUID receiverSlackId, UUID startHubId, UUID endHubId, String destinationAddress) {
		return new OrderCreatedEvent(
			order.getId(),
			receiverId,
			receiverSlackId,
			startHubId,
			endHubId,
			destinationAddress
		);
	}
}

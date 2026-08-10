package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;
// order -> delivery 배송 생성 요청
public record OrderCreatedEvent(
	UUID orderId,
	UUID receiverId,
	UUID receiverSlackId,
	UUID startHubId,
	UUID endHubId,
	String destinationAddress
) {
}

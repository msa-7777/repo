package com.msa7.v1.delivery.presentation.dto;
import java.util.UUID;

public record CreateDeliveryRequest(
	UUID orderId,
	UUID receiverSlackId,
	UUID startHubId,
	UUID endHubId,
	String destinationAddress,
	String receiverName
) {
}

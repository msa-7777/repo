package com.msa7.v1.delivery.presentation.internal.dto;

import java.util.List;
import java.util.UUID;

public record DeliveryRouteInfoResponse(
	UUID deliveryId,
	UUID startHubId,
	UUID endHubId,
	String destinationAddress,
	UUID companyManagerId,
	List<DeliveryRouteInfoDetails> routes
) {
	public record DeliveryRouteInfoDetails(
		Integer sequence,
		UUID hubManagerId,
		String status
	){}
}

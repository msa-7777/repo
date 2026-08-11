package com.msa7.v1.delivery.presentation.dto.payload;

import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.RouteStatus;

public record DeliveryRouteResponse(
	UUID routeId,
	Integer sequence,
	UUID startHubId,
	UUID endHubId,
	RouteStatus status
) {
}

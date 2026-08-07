package com.msa7.v1.delivery.presentation.dto;

import java.util.UUID;

public record HubRouteResponse(
	UUID startHubId,
	UUID endHubId,
	Long estimatedDistance,
	Long estimatedTime
) {}

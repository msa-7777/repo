package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

public record DeliveryRouteResponse(UUID routeId, Integer sequence, UUID startHubId, UUID endHubId, String status) {}

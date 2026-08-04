package com.msa7.hub.presentation.response;

import com.msa7.hub.domain.model.HubRoute;

import java.util.UUID;

public record HubRouteResponse(

    UUID hubRouteId,
    UUID fromHubId,
    UUID toHubId,
    int distance,
    int duration,
    String createdAt,
    String updatedAt

) {
    public static HubRouteResponse from(HubRoute hubRoute) {
        return new HubRouteResponse(
                hubRoute.getId(),
                hubRoute.getFromHubId(),
                hubRoute.getToHubId(),
                hubRoute.getDistance(),
                hubRoute.getDuration(),
                hubRoute.getCreatedAt().toString(),
                hubRoute.getUpdatedAt().toString()
        );
    }
}

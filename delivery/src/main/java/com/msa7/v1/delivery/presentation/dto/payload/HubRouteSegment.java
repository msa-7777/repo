package com.msa7.v1.delivery.presentation.dto.payload;

import java.util.UUID;

public record HubRouteSegment(

    int sequence,
    UUID fromHubId,
    UUID toHubId,
    int distance,
    int duration

) {
}

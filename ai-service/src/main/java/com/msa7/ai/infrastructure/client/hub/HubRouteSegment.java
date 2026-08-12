package com.msa7.ai.infrastructure.client.hub;

import java.util.UUID;

public record HubRouteSegment(

    int sequence,
    UUID fromHubId,
    UUID toHubId,
    int distance,
    int duration

) {
}

package com.msa7.hub.presentation.response;

import java.util.UUID;

public record HubRouteSegment(

    int sequence,
    UUID fromHubId,
    UUID toHubId,
    int distance,
    int duration

) {
}

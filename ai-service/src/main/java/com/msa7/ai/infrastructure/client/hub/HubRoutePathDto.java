package com.msa7.ai.infrastructure.client.hub;

import java.util.List;
import java.util.UUID;

public record HubRoutePathDto(

    int totalDistance,
    int totalDuration,
    List<Segment> segments

) {
    public record Segment(
        int sequence,
        UUID fromHubId,
        UUID toHubId,
        int distance,
        int duration
    ) {
    }
}

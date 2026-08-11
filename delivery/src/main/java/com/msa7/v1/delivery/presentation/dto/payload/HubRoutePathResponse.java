package com.msa7.v1.delivery.presentation.dto.payload;

import java.util.List;


public record HubRoutePathResponse(
    int totalDistance,
    int totalDuration,
    List<HubRouteSegment> segments
) {
    public static HubRoutePathResponse from(com.msa7.v1.delivery.presentation.dto.payload.HubRoutePathDto path) {
        List<HubRouteSegment> segments = path.segments().stream()
                .map(segment -> new HubRouteSegment(
                        segment.sequence(),
                        segment.fromHubId(),
                        segment.toHubId(),
                        segment.distance(),
                        segment.duration()
                ))
                .toList();

        return new HubRoutePathResponse(path.totalDistance(), path.totalDuration(), segments);
    }
}

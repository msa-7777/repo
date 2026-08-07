package com.msa7.hub.presentation.response;

import java.util.List;

public record HubRoutePathResponse(
    int totalDistance,
    int totalDuration,
    List<HubRouteSegment> segments
) {
}

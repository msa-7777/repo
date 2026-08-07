package com.msa7.hub.presentation.request;

import java.util.UUID;

public record HubRouteSearchRequest(

    UUID fromHubId,
    UUID toHubId

) {
}

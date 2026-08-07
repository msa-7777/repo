package com.msa7.hub.presentation.response;

import com.msa7.hub.domain.model.Hub;

import java.math.BigDecimal;
import java.util.UUID;

public record HubResponse(

    UUID hubId,
    String name,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    UUID centralHubId,
    String createdAt,
    String updatedAt

) {

    public static HubResponse from(Hub hub) {
        return new HubResponse(
            hub.getId(),
            hub.getName(),
            hub.getAddress(),
            hub.getLatitude(),
            hub.getLongitude(),
            hub.getCentralHubId(),
            hub.getCreatedAt().toString(),
            hub.getUpdatedAt().toString()
        );
    }
}

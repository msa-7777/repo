package com.msa7.v1.delivery.presentation.dto.payload;

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
}

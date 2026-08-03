package com.msa7.hub.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRequest(

    @NotBlank
    String name,

    @NotBlank
    String address,

    @NotNull
    BigDecimal latitude,

    @NotNull
    BigDecimal longitude,

    UUID centralHubId

) {
}

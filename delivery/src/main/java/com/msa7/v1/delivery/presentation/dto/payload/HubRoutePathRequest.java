package com.msa7.v1.delivery.presentation.dto.payload;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record HubRoutePathRequest(

    @NotNull(message = "출발 허브 ID는 필수 값입니다.")
    UUID fromHubId,

    @NotNull(message = "도착 허브 ID는 필수 값입니다.")
    UUID toHubId

) {
}

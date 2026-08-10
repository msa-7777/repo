package com.msa7.hub.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HubRoutePathRequest(

    @NotNull(message = "출발 허브 ID는 필수 값입니다.")
    UUID fromHubId,

    @NotNull(message = "도착 허브 ID는 필수 값입니다.")
    UUID toHubId

) {
}

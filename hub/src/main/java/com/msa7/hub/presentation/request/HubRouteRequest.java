package com.msa7.hub.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record HubRouteRequest (
        @NotNull(message = "출발 허브 ID는 필수 값입니다.")
        UUID fromHubId,

        @NotNull(message = "도착 허브 ID는 필수 값입니다.")
        UUID toHubId,

        @NotNull(message = "허브간 이동거리(km)는 필수 값입니다.")
        @Positive
        Integer distance,

        @NotNull(message = "허브간 소요시간(분)은 필수 값입니다.")
        @Positive
        Integer duration
){
}

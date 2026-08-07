package com.msa7.hub.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRequest(

    @NotBlank(message = "허브 이름은 필수 값입니다.")
    @Size(max = 50)
    String name,

    @NotBlank(message = "주소는 필수 값입니다.")
    @Size(max = 100)
    String address,

    @NotNull(message = "위도는 필수 값입니다.")
    @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.")
    BigDecimal latitude,

    @NotNull(message = "경도는 필수 값입니다.")
    @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다.")
    BigDecimal longitude,

    UUID centralHubId

) {
}

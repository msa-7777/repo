package com.sparta.productservice.presentation.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProductCreateRequest(

        @NotNull(message = "업체 ID는 필수입니다.")
        UUID companyId,

        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String name
) {
}

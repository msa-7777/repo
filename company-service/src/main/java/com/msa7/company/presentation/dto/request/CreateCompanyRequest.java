package com.msa7.company.presentation.dto.request;

import com.msa7.company.domain.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank(message = "업체명은 필수 입력 항목입니다.")
        @Size(max = 100, message = "업체명은 최대 100자까지 입력 가능합니다.")
        String name,

        @NotNull(message = "업체 타입은 필수 입력 항목입니다.")
        CompanyType type,

        @NotNull(message = "허브 ID는 필수 입력 항목입니다.")
        UUID hubId,

        @NotBlank(message = "주소는 필수 입력 항목입니다.")
        String address
) {
}
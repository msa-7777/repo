package com.msa7.company.presentation.dto.request;

import com.msa7.company.domain.model.CompanyType;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCompanyRequest(
        @Size(max = 100, message = "업체명은 최대 100자까지 입력 가능합니다.")
        String name,

        CompanyType type,

        UUID hubId,

        String address
) {
}
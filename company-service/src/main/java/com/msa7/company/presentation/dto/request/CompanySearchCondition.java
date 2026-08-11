package com.msa7.company.presentation.dto.request;

import com.msa7.company.domain.model.CompanyType;

import java.util.UUID;

public record CompanySearchCondition(
        String name,
        CompanyType type,
        UUID hubId
) {
}
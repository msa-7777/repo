package com.msa7.company.domain.model;

import java.util.UUID;

public record CompanySearchCondition(
        String name,
        CompanyType type,
        UUID hubId
) {
}
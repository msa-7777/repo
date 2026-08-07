package com.sparta.productservice.domain.product;

import java.util.UUID;

public record ProductSearchCondition(
        // 검색 조건 DTO
        String name,
        UUID companyId
) {
}
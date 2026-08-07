package com.sparta.productservice.presentation.product.response;

import com.sparta.productservice.domain.product.Product;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID productId,
        UUID companyId,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCompanyId(),
                product.getName(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
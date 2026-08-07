package com.sparta.productservice.presentation.inventory.response;

import com.sparta.productservice.domain.inventory.Inventory;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        int quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    // Inventory 엔티티를 외부 응답 DTO로 변환한다.
    // Entity를 Controller에서 직접 반환하지 않도록 분리한다.
    public static InventoryResponse from(
            Inventory inventory
    ) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getHubId(),
                inventory.getQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}
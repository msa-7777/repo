package com.sparta.productservice.domain.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository
        extends JpaRepository<Inventory, UUID>, InventoryRepositoryCustom {

    /**
     * 상품 ID를 기준으로 논리 삭제되지 않은 재고를 조회한다.
     *
     * GET /api/v1/inventories/{productId}
     * PATCH /api/v1/inventories/{productId}/quantity
     * 두 기능에서 공통으로 사용한다.
     */
    Optional<Inventory> findByProductIdAndDeletedAtIsNull(UUID productId);

    /**
     * 해당 상품에 활성 재고가 이미 존재하는지 확인한다.
     * 상품 하나당 활성 재고 하나만 생성하기 위한 검사다.
     */
    boolean existsByProductIdAndDeletedAtIsNull(UUID productId);
}
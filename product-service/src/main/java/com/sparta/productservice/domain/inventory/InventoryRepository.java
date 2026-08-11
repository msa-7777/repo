package com.sparta.productservice.domain.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

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

    // 재고 수량 변경을 위해 활성 재고를 비관적 쓰기 락으로 조회한다.
    /*
     * 동일 상품의 재고를 동시에 변경하려는 트랜잭션이 존재하면
     * 먼저 락을 획득한 트랜잭션이 종료될 때까지 다른 트랜잭션은 대기한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
        FROM Inventory i
        WHERE i.productId = :productId
          AND i.deletedAt IS NULL
        """)
    Optional<Inventory> findByProductIdForUpdate(UUID productId);


    /**
     * 해당 상품에 활성 재고가 이미 존재하는지 확인한다.
     * 상품 하나당 활성 재고 하나만 생성하기 위한 검사다.
     */
    boolean existsByProductIdAndDeletedAtIsNull(UUID productId);

}
package com.sparta.productservice.domain.inventory;

import com.sparta.productservice.global.audit.BaseEntity;
import com.sparta.productservice.global.exception.ApiException;
import com.sparta.productservice.global.exception.inventory.InventoryErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "p_inventories",
        indexes = {
                /* 상품 ID로 재고를 단건 조회하는 요청이 많을 것으로 예상
                 * product_id에 인덱스를 설정한다.*/
                @Index(name = "idx_inventories_product", columnList = "product_id"),

                // 허브별 재고 검색에 사용한다.
                @Index(name = "idx_inventories_hub", columnList = "hub_id"),

                // 최소·최대 재고 수량 검색에 사용한다.
                @Index(name = "idx_inventories_quantity", columnList = "quantity")
        }
)
public class Inventory extends BaseEntity {

    @Id
    @Column(name = "inventory_id", nullable = false, updatable = false)
    private UUID id;

    // 재고 대상 상품 ID.
    // Product와 Inventory가 같은 서비스에 있지만, 현재 설계에서는 엔티티 연관관계 대신 UUID만 저장한다.
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    // 재고가 보관되는 허브 ID.
    // 상품 생성 시 company-service에서 조회한 업체 소속 hubId를 저장한다.
    @Column(name = "hub_id", nullable = false, updatable = false)
    private UUID hubId;

    // 현재 재고 수량.
    // 상품이 생성될 때 0으로 시작하며, 음수가 될 수 없다.
    @Column(nullable = false)
    private int quantity;

    private Inventory(
            UUID id,
            UUID productId,
            UUID hubId,
            int quantity
    ) {
        this.id = id;
        this.productId = productId;
        this.hubId = hubId;
        this.quantity = quantity;
    }

    // 상품 생성과 함께 초기 재고를 생성한다.
    // 초기 수량은 확정된 정책에 따라 항상 0이다.
    public static Inventory create(
            UUID productId,
            UUID hubId
    ) {
        return new Inventory(
                UUID.randomUUID(),
                productId,
                hubId,
                0
        );
    }

    // 입고 또는 주문 취소 복구로 재고를 증가시킨다.
    public void increase(int amount) {
        validatePositiveAmount(amount);

        this.quantity += amount;
    }

    // 주문 생성으로 재고를 차감한다.
    // 현재 재고보다 많은 수량을 요청하면 재고를 음수로 만들지 않고 예외를 발생시킨다.
    public void decrease(int amount) {
        validatePositiveAmount(amount);

        if (this.quantity < amount) {
            throw new ApiException(
                    InventoryErrorCode.INSUFFICIENT_INVENTORY
            );
        }

        this.quantity -= amount;
    }

    // 변경 수량은 반드시 1 이상이어야 한다.
     /* DTO에서도 @Positive로 검증하지만,
     * 내부 서비스 호출이나 직접 메서드 호출에 대비해 도메인 엔티티에서도 다시 검증한다.
     */
    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new ApiException(
                    InventoryErrorCode.INVALID_CHANGE_QUANTITY
            );
        }
    }
}

package com.sparta.productservice.application.inventory;

import com.sparta.productservice.domain.inventory.Inventory;
import com.sparta.productservice.domain.inventory.InventoryRepository;
import com.sparta.productservice.domain.inventory.InventorySearchCondition;
import com.sparta.productservice.global.exception.ApiException;
import com.sparta.productservice.global.exception.inventory.InventoryErrorCode;
import com.sparta.productservice.presentation.inventory.request.InventoryQuantityChangeRequest;
import com.sparta.productservice.presentation.inventory.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // 상품 생성 과정에서 호출되는 초기 재고 생성 메서드.
    /* 외부 Controller에는 재고 생성 API를 노출하지 않는다.
     * 초기 수량은 Inventory.create()에서 0으로 설정된다.
     */
    @Transactional
    public Inventory createInventory(
            UUID productId,
            UUID hubId
    ) {
        validateInventoryNotExists(productId);

        Inventory inventory = Inventory.create(productId, hubId);

        return inventoryRepository.save(inventory);
    }

    // 상품 ID를 기준으로 재고를 단건 조회한다.
    public InventoryResponse getInventory(UUID productId) {
        Inventory inventory = findActiveInventory(productId);

        return InventoryResponse.from(inventory);
    }

    // 재고 목록을 검색 조건과 페이징 조건으로 조회한다.
    public Page<InventoryResponse> getInventories(
            InventorySearchCondition condition,
            Pageable pageable
    ) {
        validateQuantityRange(condition);

        return inventoryRepository
                .searchInventories(condition, pageable)
                .map(InventoryResponse::from);
    }

    // 입고, 주문 차감, 주문 취소 복구를 하나의 메서드에서 처리한다.
    @Transactional
    public InventoryResponse changeQuantity(
            UUID productId,
            InventoryQuantityChangeRequest request
    ) {
        Inventory inventory = findActiveInventory(productId);

        /*
         * 수량 증감 규칙은 Inventory 엔티티에 위임한다.
         *
         * Service는 어떤 업무 유형이 증가 또는 감소인지 판단하고,
         * Entity는 실제 수량 변경과 부족 재고 검증을 담당한다.
         */
        switch (request.changeType()) {
            case INBOUND, ORDER_CANCEL_RESTORE ->
                    inventory.increase(request.quantity());

            case ORDER_DECREASE ->
                    inventory.decrease(request.quantity());
        }

        /*
         * inventory는 영속 상태이므로 save()를 다시 호출하지 않아도
         * 트랜잭션 종료 시 변경 감지로 UPDATE 쿼리가 실행된다.
         */

        // TODO: 인증 적용 후 INBOUND는 HUB_MANAGER와 MASTER만 허용한다.
        /* TODO: ORDER_DECREASE와 ORDER_CANCEL_RESTORE는 order-service의 내부 호출만 허용한다.
        */
        // TODO: 동시 주문 시 재고 정합성을 위해 비관적 락, 낙관적 락 또는 조건부 UPDATE를 적용한다.

        return InventoryResponse.from(inventory);
    }

    // 삭제되지 않은 재고를 상품 ID로 조회한다.
    private Inventory findActiveInventory(UUID productId) {
        return inventoryRepository
                .findByProductIdAndDeletedAtIsNull(productId)
                .orElseThrow(() ->
                        new ApiException(InventoryErrorCode.INVENTORY_NOT_FOUND));
    }

    // 같은 상품에 활성 재고가 중복 생성되는 것을 방지한다.
    private void validateInventoryNotExists(UUID productId) {
        boolean exists = inventoryRepository
                        .existsByProductIdAndDeletedAtIsNull(productId);

        if (exists) {
            throw new ApiException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS);
        }
    }

    // 최소 수량과 최대 수량이 모두 전달되면 검색 범위가 올바른지 검증한다.
    private void validateQuantityRange(
            InventorySearchCondition condition
    ) {
        if (condition.minQuantity() != null
                && condition.maxQuantity() != null
                && condition.minQuantity()
                > condition.maxQuantity()) {

            throw new ApiException(InventoryErrorCode.INVALID_QUANTITY_RANGE);
        }
    }


    /**
     * 상품 삭제 시 연결된 활성 재고가 존재하면 함께 논리 삭제한다.
     *
     * 재고가 없는 상품은 과거 데이터나 비정상 데이터일 수 있으므로 상품 삭제 자체를 실패시키지는 않는다.
     */
    @Transactional
    public void deleteInventoryIfExists(
            UUID productId,
            UUID deletedBy
    ) {
        inventoryRepository
                .findByProductIdAndDeletedAtIsNull(productId)
                .ifPresent(inventory ->
                        inventory.delete(deletedBy)
                );
    }


    // 상품 접근 권한 검증을 위해 해당 상품의 활성 재고가 속한 허브 ID를 반환한다.
    public UUID getHubIdByProductId(UUID productId) {
        return findActiveInventory(productId).getHubId();
    }
}

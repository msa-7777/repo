package com.sparta.productservice.domain.inventory;

import java.util.UUID;

/* 재고 목록 및 검색에 사용하는 동적 검색 조건.
 * 모든 값은 선택 입력이며 null이면 해당 조건을 적용하지 않는다.
 */
public record InventorySearchCondition(
        UUID hubId,
        Integer minQuantity,
        Integer maxQuantity
) {
}
package com.sparta.productservice.domain.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryRepositoryCustom {

    // Querydsl을 이용해 활성 재고를 동적으로 검색한다.
    Page<Inventory> searchInventories(
            InventorySearchCondition condition,
            Pageable pageable
    );
}

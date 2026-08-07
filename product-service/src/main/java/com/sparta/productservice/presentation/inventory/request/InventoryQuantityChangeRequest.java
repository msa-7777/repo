package com.sparta.productservice.presentation.inventory.request;

import com.sparta.productservice.domain.inventory.InventoryChangeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryQuantityChangeRequest(

        /* 입고, 주문 차감, 주문 취소 복구 중 어떤 이유로 재고가 변경되는지 지정한다.
         */
        @NotNull(message = "재고 변경 유형은 필수입니다.")
        InventoryChangeType changeType,

        // 변경할 수량 자체는 항상 양수로 받는다.
        // 증가·감소 여부는 quantity의 부호가 아니라 changeType으로 판단한다.
        @Positive(message = "변경 수량은 1 이상이어야 합니다.")
        int quantity
) {
}

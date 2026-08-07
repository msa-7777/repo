package com.sparta.productservice.domain.inventory;

/**
 * 재고 수량이 변경되는 업무상의 이유를 나타낸다.
 *
 * 단순히 양수·음수를 받지 않고 변경 유형을 명시함으로써
 * 입고, 주문 차감, 주문 취소 복구를 구분한다.
 */

public enum InventoryChangeType {


     // 입고 처리 - 재고 수량을 증가
    INBOUND,

    // 주문 생성에 따른 재고 차감
    ORDER_DECREASE,

    //주문 취소에 따른 재고 복구
    ORDER_CANCEL_RESTORE
}

package com.sparta.userservice.global.exception.inventory;

import com.sparta.productservice.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    // productId에 해당하는 활성 재고가 없음
    INVENTORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "INVENTORY_NOT_FOUND",
            "재고 정보를 찾을 수 없습니다."
    ),

    // 같은 상품에 활성 재고가 이미 존재함
    INVENTORY_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "INVENTORY_ALREADY_EXISTS",
            "해당 상품의 재고가 이미 존재합니다."
    ),

    // 변경 요청 수량이 0 이하임
    INVALID_CHANGE_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "INVALID_CHANGE_QUANTITY",
            "재고 변경 수량은 1 이상이어야 합니다."
    ),

    // 현재 수량보다 많이 차감하려 함
    INSUFFICIENT_INVENTORY(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_INVENTORY",
            "재고 수량이 부족합니다."
    ),

    // minQuantity > maxQuantity
    INVALID_QUANTITY_RANGE(
            HttpStatus.BAD_REQUEST,
            "INVALID_QUANTITY_RANGE",
            "최소 재고 수량은 최대 재고 수량보다 클 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
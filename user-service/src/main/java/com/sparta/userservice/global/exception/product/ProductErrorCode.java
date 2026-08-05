package com.sparta.userservice.global.exception.product;

import com.sparta.productservice.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PRODUCT_NOT_FOUND",
            "상품을 찾을 수 없습니다."
    ),
    PRODUCT_NAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "PRODUCT_NAME_DUPLICATED",
                    "동일한 업체에 같은 이름의 상품이 이미 존재합니다."
    ),

    COMPANY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMPANY_NOT_FOUND",
            "상품을 등록할 업체를 찾을 수 없습니다."
    ),

    INVALID_COMPANY_TYPE(
            HttpStatus.BAD_REQUEST,
            "INVALID_COMPANY_TYPE",
            "생산업체만 상품을 등록할 수 있습니다."
    ),

    COMPANY_HUB_NOT_FOUND(
            HttpStatus.CONFLICT,
            "COMPANY_HUB_NOT_FOUND",
            "업체의 소속 허브 정보를 확인할 수 없습니다."
    ),

    COMPANY_SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "COMPANY_SERVICE_UNAVAILABLE",
            "업체 서비스에 일시적으로 연결할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
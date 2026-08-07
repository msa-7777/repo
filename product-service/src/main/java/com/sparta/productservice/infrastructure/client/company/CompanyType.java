package com.sparta.productservice.infrastructure.client.company;

// company-service 업체 단건 조회 응답의 업체 유형.
public enum CompanyType {

    // 생산업체 - 현재 정책상 상품 생성 가능.
    PRODUCER,

    // 수령업체 - 현재 정책상 상품 생성 불가.
    RECEIVER
}
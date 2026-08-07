package com.sparta.productservice.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    // 커스텀 Repository 인터페이스

    /* 상품 검색 조건과 페이징 정보를 이용하여 삭제되지 않은 상품을 동적으로 검색한다.
     *
     * 검색 조건:
     * - 상품명 부분 일치
     * - 업체 ID 일치
     * - deletedAt이 null인 활성 상품만 조회
     */
    Page<Product> searchProducts(
            ProductSearchCondition condition,
            Pageable pageable
    );
}

package com.sparta.productservice.infrastructure.persistence.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.productservice.domain.product.Product;
import com.sparta.productservice.domain.product.ProductRepositoryCustom;
import com.sparta.productservice.domain.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.sparta.productservice.domain.product.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    // Querydsl 구현체

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        /*
         * 논리 삭제되지 않은 상품만 조회한다.
         * 검색 조건 유무와 관계없이 항상 적용되어야 한다.
         */
        builder.and(product.deletedAt.isNull());

        /*
         * 상품명이 null 또는 빈 문자열이 아니면
         * 상품명 부분 일치 조건을 추가한다.
         *
         * containsIgnoreCase를 사용하므로 대소문자를 구분하지 않고
         * 상품명에 검색어가 포함된 상품을 조회한다.
         */
        if (StringUtils.hasText(condition.name())) {
            builder.and(
                    product.name.containsIgnoreCase(condition.name())
            );
        }

        /*
         * companyId가 전달된 경우
         * 해당 업체의 상품만 조회한다.
         */
        if (condition.companyId() != null) {
            builder.and(
                    product.companyId.eq(condition.companyId())
            );
        }

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(builder)
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0L
        );
    }
}

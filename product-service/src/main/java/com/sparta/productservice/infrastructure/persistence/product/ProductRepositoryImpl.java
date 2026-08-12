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
import java.util.UUID;

import static com.sparta.productservice.domain.inventory.QInventory.inventory;
import static com.sparta.productservice.domain.product.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    // Querydsl 구현체

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(
            ProductSearchCondition condition,
            Pageable pageable,
            UUID accessibleHubId
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 논리 삭제되지 않은 상품만 조회한다.
        builder.and(product.deletedAt.isNull());

        // 상품명이 전달되면 대소문자를 구분하지 않고 부분 일치 검색한다.
        if (StringUtils.hasText(condition.name())) {
            builder.and(
                    product.name.containsIgnoreCase(condition.name())
            );
        }

        // 업체 ID가 전달되면 해당 업체의 상품만 조회한다.
        if (condition.companyId() != null) {
            builder.and(
                    product.companyId.eq(condition.companyId())
            );
        }

        // HUB_MANAGER의 경우 담당 허브에 속한 상품만 조회한다.
        /*
         * 상품에는 hubId가 없으므로 Inventory의 productId와 Product의 id를 연결하고,
         * Inventory의 hubId가 HUB_MANAGER의 담당 hubId와 같은 상품만 조회한다.
         *
         * accessibleHubId가 null이면
         * MASTER, SUPPLIER_AGENT, DELIVERY_AGENT이므로 허브 조건을 적용하지 않는다.
         */
        if (accessibleHubId != null) {
            builder.and(product.id.in(
                            queryFactory.select(inventory.productId)
                                        .from(inventory)
                                        .where(
                                            inventory.hubId.eq(accessibleHubId),
                                            inventory.deletedAt.isNull()
                                        )
            ));
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

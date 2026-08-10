package com.sparta.productservice.infrastructure.persistence.inventory;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.productservice.domain.inventory.Inventory;
import com.sparta.productservice.domain.inventory.InventoryRepositoryCustom;
import com.sparta.productservice.domain.inventory.InventorySearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.productservice.domain.inventory.QInventory.inventory;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {
    // Querydsl 구현체

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Inventory> searchInventories(
            InventorySearchCondition condition,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 논리 삭제되지 않은 재고만 조회한다.
        // 검색 조건과 관계없이 항상 적용한다.
        builder.and(inventory.deletedAt.isNull());

        // hubId가 전달되면 해당 허브의 재고만 조회한다.
        if (condition.hubId() != null) {
            builder.and(
                    inventory.hubId.eq(condition.hubId())
            );
        }

        // 최소 수량이 전달되면 quantity >= minQuantity 조건을 추가한다.
        if (condition.minQuantity() != null) {
            builder.and(
                    inventory.quantity.goe(
                            condition.minQuantity()
                    )
            );
        }

        // 최대 수량이 전달되면 quantity <= maxQuantity 조건을 추가한다.
        if (condition.maxQuantity() != null) {
            builder.and(
                    inventory.quantity.loe(
                            condition.maxQuantity()
                    )
            );
        }

        List<Inventory> content = queryFactory
                .selectFrom(inventory)
                .where(builder)
                //정렬을 생성일 내림차순으로 고정한다.

                 /* TODO: 팀 API 명세에 따라 Pageable의 허용 정렬 필드를
                 *       createdAt, quantity 등으로 제한하여 적용한다.
                 */
                .orderBy(inventory.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(inventory.count())
                .from(inventory)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0L
        );
    }
}
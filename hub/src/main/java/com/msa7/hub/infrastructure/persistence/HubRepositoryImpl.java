package com.msa7.hub.infrastructure.persistence;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.model.QHub;
import com.msa7.hub.domain.repository.HubRepositoryCustom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QHub hub = QHub.hub;

    @Override
    public Page<Hub> search(String name, String address, Boolean isCentral, Pageable pageable) {
        List<Hub> content = queryFactory
                .selectFrom(hub)
                .where(
                        hub.deletedAt.isNull(),
                        nameContains(name),
                        addressContains(address),
                        isCentral(isCentral)
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(hub.count())
                .from(hub)
                .where(
                        hub.deletedAt.isNull(),
                        nameContains(name),
                        addressContains(address),
                        isCentral(isCentral)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? hub.name.contains(name) : null;
    }

    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? hub.address.contains(address) : null;
    }

    private BooleanExpression isCentral(Boolean isCentral) {
        if (isCentral == null) {
            return null;
        }
        // 중앙 허브는 centralHubId null
        return isCentral ? hub.centralHubId.isNull() : hub.centralHubId.isNotNull();
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        OrderSpecifier<?>[] specifiers = sort.stream()
                .map(this::toOrderSpecifier)
                .filter(Objects::nonNull)
                .toArray(OrderSpecifier[]::new);

        return specifiers.length > 0
                ? specifiers
                : new OrderSpecifier[]{hub.createdAt.desc(), hub.updatedAt.desc()};
    }

    // 정렬 가능한 필드를  생성일순, 수정일순을 기준으로 제한
    private OrderSpecifier<?> toOrderSpecifier(Sort.Order sortOrder) {
        Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
        return switch(sortOrder.getProperty()) {
            case "createdAt" -> new OrderSpecifier<>(direction, hub.createdAt);
            case "updatedAt" -> new OrderSpecifier<>(direction, hub.updatedAt);
            default -> null;
        };
    }
}

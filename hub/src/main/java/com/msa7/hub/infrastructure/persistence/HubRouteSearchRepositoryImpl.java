package com.msa7.hub.infrastructure.persistence;

import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.domain.model.QHubRoute;
import com.msa7.hub.domain.repository.HubRouteSearchRepository;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRouteSearchRepositoryImpl implements HubRouteSearchRepository {

    private final JPAQueryFactory queryFactory;
    private final QHubRoute hubRoute = QHubRoute.hubRoute;

    @Override
    public Page<HubRoute> search(UUID fromHubId, UUID toHubId, Pageable pageable) {
        List<HubRoute> content = queryFactory.selectFrom(hubRoute)
                .where(
                        hubRoute.deletedAt.isNull(),
                        fromHubIdEq(fromHubId),
                        toHubIdEq(toHubId)
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(hubRoute.count())
                .from(hubRoute)
                .where(
                        hubRoute.deletedAt.isNull(),
                        fromHubIdEq(fromHubId),
                        toHubIdEq(toHubId)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression fromHubIdEq(UUID fromHubId) {
        return fromHubId != null ? hubRoute.fromHubId.eq(fromHubId) : null;
    }

    private BooleanExpression toHubIdEq(UUID toHubId) {
        return toHubId != null ? hubRoute.toHubId.eq(toHubId) : null;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        OrderSpecifier<?>[] specifiers = sort.stream()
                .map(this::toOrderSpecifier)
                .filter(Objects::nonNull)
                .toArray(OrderSpecifier[]::new);

        return specifiers.length > 0
                ? specifiers
                : new OrderSpecifier[]{hubRoute.createdAt.desc(), hubRoute.updatedAt.desc()};
    }

    // 정렬 가능한 필드를 생성일순, 수정일순을 기준으로 제한
    private OrderSpecifier<?> toOrderSpecifier(Sort.Order sortOrder) {
        Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
        return switch (sortOrder.getProperty()) {
            case "createdAt" -> new OrderSpecifier<>(direction, hubRoute.createdAt);
            case "updatedAt" -> new OrderSpecifier<>(direction, hubRoute.updatedAt);
            default -> null;
        };
    }
}

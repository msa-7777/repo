package com.sparta.userservice.Infrastructure.persistence.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.userservice.application.query.UserSearchCondition;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import com.sparta.userservice.domain.model.QUser; // Q class 사용
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 설명: Querydsl을 사용하여 사용자 검색, 페이징 및 다중 정렬 조회를 수행하는 Repository
 * { Q class : Querydsl이 엔티티를 조회 조건으로 사용할 수 있게 자동 생성해주는 클래스 }

 UserSearchRequest
 ├── 검색: loginId, name, role, keyword
 ├── 페이징: page, size
 └── 정렬: sortBy, sort
        ↓
 AdminUserService
 ├── UserSearchCondition 생성
 └── PageUtil로 Pageable 생성
        ↓
 UserRepositoryImpl
 ├── 검색 조건 적용
 ├── offset/limit 적용
 └── 정렬 조건으로 Querydsl 정렬
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 전달된 검색 조건을 기준으로 승인되어 있으면서, 삭제되지 않은 회원 목록을 조회합니다.
     *
     * 개별 검색 조건은 AND로 연결되며,
     * keyword 검색 내부에서는 loginId와 name 조건이 OR로 연결됩니다.
     *
     * @param condition 회원 검색 조건
     * @param pageable 페이징 및 정렬 조건
     * @return 검색된 회원 페이지
     */
    @Override
    public Page<User> searchApprovedUsers(UserSearchCondition condition, Pageable pageable) {
        QUser user = QUser.user;

        // hubId, supplierId는 다른 MSA 서비스 Entity의 식별자만 UUID로 저장하므로
        // JPA 연관관계 및 Join을 사용하지 않는다.
        // 단순 컬럼 조회이므로 JPA N+1 문제는 발생하지 않는다.
        BooleanExpression[] conditions = { // where절 공통 부분 묶기
                loginIdContains(user, condition),
                nameContains(user, condition),
                keywordContains(user, condition),
                roleEq(user, condition),
                user.signupStatus.eq(SignupStatus.APPROVED), // 승인된 user만 검색
                user.isDeleted.isFalse() // soft delete 되지 않은 것만 검색
        };

        List<User> users = queryFactory
                .selectFrom(user)
                .where(conditions)
                .orderBy(toOrderSpecifiers(user, pageable))
                .offset(pageable.getOffset()) // .offset((long) userSearch.getPage() * userSearch.getSize())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count()) // .select(Wildcard.count)
                .from(user)
                .where(conditions);

        return PageableExecutionUtils.getPage(
                users,
                pageable,
                countQuery::fetchOne // () -> { Long count = countQuery.fetchOne(); return count == null ? 0L : count; }
        );
    }

    @Override
    public Page<User> searchSignupRequests(UserSearchCondition condition, Pageable pageable, UUID hubId) {
        QUser user = QUser.user;

        BooleanExpression[] conditions = {
                loginIdContains(user, condition),
                nameContains(user, condition),
                keywordContains(user, condition),
                roleEq(user, condition),
                hubIdEq(user, hubId),
                user.signupStatus.eq(SignupStatus.PENDING), // 승인 대기 중인 user만 검색
                user.isDeleted.isFalse() // soft delete 되지 않은 것만 검색
        };

        List<User> users = queryFactory
                .selectFrom(user)
                .where(conditions)
                .orderBy(toOrderSpecifiers(user, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(conditions);

        return PageableExecutionUtils.getPage(
                users,
                pageable,
                countQuery::fetchOne
        );
    }






    /**
     * 로그인 아이디 부분 검색 조건을 생성합니다.
     */
    private BooleanExpression loginIdContains(QUser user, UserSearchCondition condition) {
        if (condition == null || isNullOrBlank(condition.getLoginId())) {
            return null;
        }
        return user.loginId.containsIgnoreCase(condition.getLoginId().trim());
    }

    /**
     * 사용자 이름 부분 검색 조건을 생성합니다.
     */
    private BooleanExpression nameContains(QUser user, UserSearchCondition condition) {
        if (condition == null || isNullOrBlank(condition.getName())) {
            return null;
        }
        return user.name.containsIgnoreCase(condition.getName().trim());
    }

    /**
     * 검색어를 기준으로 로그인 아이디 또는 이름 통합 검색 조건을 생성합니다.
     */
    private BooleanExpression keywordContains(QUser user, UserSearchCondition condition) {
        if (condition == null || isNullOrBlank(condition.getKeyword())) {
            return null;
        }
        String keyword = condition.getKeyword().trim();

        return user.loginId.containsIgnoreCase(keyword)
                .or(user.name.containsIgnoreCase(keyword));
    }

    /**
     * 사용자 역할 일치 조건을 생성합니다.
     */
    private BooleanExpression roleEq(QUser user, UserSearchCondition condition) {
        if (condition == null || condition.getRole() == null) {
            return null;
        }

        return user.role.eq(condition.getRole());
    }

    /**
     * Pageable의 모든 정렬 조건을 Querydsl 정렬 조건으로 변환합니다.
     * 지원하는 정렬 조건이 없으면 생성일 내림차순을 기본값으로 적용합니다.
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(QUser user, Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (pageable != null && pageable.getSort().isSorted()) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                Order direction = sortOrder.isAscending()
                        ? Order.ASC
                        : Order.DESC;

                OrderSpecifier<?> orderSpecifier = createOrderSpecifier(
                        user,
                        sortOrder.getProperty(),
                        direction
                );

                if (orderSpecifier != null) {
                    orderSpecifiers.add(orderSpecifier);
                }
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(user.createdAt.desc()); // 정렬 조건이 없다면 생성일 기준으로 정렬
        }

        return orderSpecifiers.toArray(OrderSpecifier<?>[]::new);
    }

    /**
     * 허용된 회원 필드에 대해서만 Querydsl 정렬 조건을 생성합니다.
     */
    private OrderSpecifier<?> createOrderSpecifier(QUser user, String property, Order direction) {
        return switch (property) {
            case "loginId" ->
                    new OrderSpecifier<>(direction, user.loginId);

            case "name" ->
                    new OrderSpecifier<>(direction, user.name);

            case "role" ->
                    new OrderSpecifier<>(direction, user.role);

            case "createdAt" ->
                    new OrderSpecifier<>(direction, user.createdAt);

            case "updatedAt" ->
                    new OrderSpecifier<>(direction, user.updatedAt);

            default -> null;
        };
    }

    /**
     *
     * @param user
     * @param hubId
     * @return
     */
    private BooleanExpression hubIdEq(QUser user, UUID hubId) {

        if (hubId == null) {
            return null;
        }

        return user.hubId.eq(hubId);
    }

    private boolean isNullOrBlank(String value) { return value == null || value.isBlank(); }
}
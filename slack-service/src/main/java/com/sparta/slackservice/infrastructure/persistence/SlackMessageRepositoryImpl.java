package com.sparta.slackservice.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.slackservice.domain.QSlackMessage;
import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageRepositoryCustom;
import com.sparta.slackservice.domain.SlackMessageSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SlackMessageRepositoryImpl implements SlackMessageRepositoryCustom {
    // Querydsl 구현체

    // Querydsl 쿼리를 생성하고 실행하는 객체
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SlackMessage> search(
            SlackMessageSearchCondition condition,
            Pageable pageable
    ) {
        // QSlackMessage는 SlackMessage 엔티티를 기반으로 Querydsl이 자동 생성한 메타 모델 클래스
        QSlackMessage slackMessage = QSlackMessage.slackMessage;

        // 요청으로 전달된 검색 조건을 BooleanBuilder로 변환한다.
        // 값이 없는 검색 조건은 쿼리에 포함하지 않는다.
        BooleanBuilder builder = createSearchCondition(
                slackMessage,
                condition
        );

        List<SlackMessage> content = queryFactory
                .selectFrom(slackMessage)
                .where(builder)
                .orderBy(slackMessage.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(slackMessage.count())
                .from(slackMessage)
                .where(builder)
                .fetchOne();

        // 조회 결과를 Spring Data의 Page 객체로 변환
        return new PageImpl<>(
                content,
                pageable,
                Optional.ofNullable(total).orElse(0L)
        );
    }

    /**
     * 검색 요청값을 Querydsl 조건으로 변환한다.
     *
     * BooleanBuilder는 조건을 순차적으로 추가할 수 있어 선택적인 검색 조건을 구현할 때 사용
     */
    private BooleanBuilder createSearchCondition(
            QSlackMessage slackMessage,
            SlackMessageSearchCondition condition
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 논리 삭제된 메시지는 모든 조회 및 검색에서 제외한다.
        // 다른 검색 조건이 없더라도 이 조건은 항상 적용된다.
        builder.and(slackMessage.deletedAt.isNull());

        // 검색 조건 객체 자체가 없으면 논리 삭제 제외 조건만 적용해 전체 목록을 조회
        if (condition == null) {
            return builder;
        }

        // 주문 ID가 전달된 경우 해당 주문의 발송 이력만 조회
        if (condition.orderId() != null) {
            builder.and(
                    slackMessage.orderId.eq(condition.orderId())
            );
        }

        // 허브 ID가 전달된 경우 해당 허브의 발송 이력만 조회
        if (condition.hubId() != null) {
            builder.and(
                    slackMessage.hubId.eq(condition.hubId())
            );
        }

        // 수신 담당자 ID가 전달된 경우 정확히 일치하는 이력만 조회
        if (condition.receiverId() != null) {
            builder.and(
                    slackMessage.receiverId.eq(condition.receiverId())
            );
        }

        // 수신 담당자 이름은 부분 일치 검색을 적용한다.
        // 대소문자 구분 없이 검색
        if (StringUtils.hasText(condition.receiverName())) {
            builder.and(
                    slackMessage.receiverName.containsIgnoreCase(
                            condition.receiverName()
                    )
            );
        }

        // SENT, FAILED, MODIFIED 중 요청한 상태와 일치하는 이력만 조회
        if (condition.status() != null) {
            builder.and(
                    slackMessage.status.eq(condition.status())
            );
        }

        // 시작 일시가 있으면 createdAt이 시작 일시 이상인 데이터만 조회
        if (condition.startDate() != null) {
            builder.and(
                    slackMessage.createdAt.goe(condition.startDate())
            );
        }

        // 종료 일시가 있으면 createdAt이 종료 일시 이하인 데이터만 조회
        if (condition.endDate() != null) {
            builder.and(
                    slackMessage.createdAt.loe(condition.endDate())
            );
        }

        return builder;
    }
}

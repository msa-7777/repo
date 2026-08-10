package com.msa7.ai.infrastructure.persistence;

import com.msa7.ai.domain.model.AiHistory;
import com.msa7.ai.domain.repository.AiHistoryRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.msa7.ai.domain.model.QAiHistory.aiHistory;

@Repository
@RequiredArgsConstructor
public class AiHistoryRepositoryImpl implements AiHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AiHistory> searchAiHistories(UUID orderId, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(aiHistory.deletedAt.isNull());

        if (orderId != null) {
            builder.and(aiHistory.orderId.eq(orderId));
        }

        List<AiHistory> content = queryFactory
                .selectFrom(aiHistory)
                .where(builder)
                .orderBy(aiHistory.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(aiHistory.count())
                .from(aiHistory)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
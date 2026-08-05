package com.msa7.company.infrastructure.persistence;

import com.msa7.company.domain.model.Company;
import com.msa7.company.domain.model.CompanySearchCondition;
import com.msa7.company.domain.repository.CompanyRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.msa7.company.domain.model.QCompany.company;

@Repository
@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Company> searchCompanies(CompanySearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // Soft Delete 조건: 삭제되지 않은 데이터만 조회
        builder.and(company.deletedAt.isNull());

        // 동적 검색 조건 처리
        if (condition != null) {
            if (StringUtils.hasText(condition.name())) {
                builder.and(company.name.containsIgnoreCase(condition.name()));
            }
            if (condition.type() != null) {
                builder.and(company.type.eq(condition.type()));
            }
            if (condition.hubId() != null) {
                builder.and(company.hubId.eq(condition.hubId()));
            }
        }

        // 조회 데이터(Content) 쿼리
        List<Company> content = queryFactory
                .selectFrom(company)
                .where(builder)
                .orderBy(company.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수(Total Count) 쿼리
        Long total = queryFactory
                .select(company.count())
                .from(company)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
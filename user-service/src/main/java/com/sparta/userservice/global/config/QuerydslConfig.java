/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: Querydsl Repository들에서 JPAQueryFactory를 편하게 주입받아 동적 쿼리를 작성할 수 있도록 공통 설정하는 것
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
package com.sparta.userservice.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}

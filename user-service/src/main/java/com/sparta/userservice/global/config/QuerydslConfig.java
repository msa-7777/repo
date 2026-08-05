package com.sparta.userservice.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {
    // Querydsl 설정 클래스

    /* Querydsl 쿼리를 작성할 때 사용하는 JPAQueryFactory를 Bean으로 등록한다.
     *
     * EntityManager는 Spring이 관리하며,
     * JPAQueryFactory는 여러 Repository 구현체에서 주입받아 사용할 수 있다.
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(
            EntityManager entityManager
    ) {
        return new JPAQueryFactory(entityManager);
    }
}

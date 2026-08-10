package com.sparta.userservice.domain.repository;

import com.sparta.userservice.application.query.UserSearchCondition;
import com.sparta.userservice.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명 : Spring Data JPA의 메서드 이름만으로 처리하기 어려운 동적 리뷰 검색 기능을 선언한다.
 *         실제 QueryDsl 동작은 UserRepositoryImpl에서 상속받아 구현
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

public interface UserRepositoryCustom {
    Page<User> searchApprovedUsers(UserSearchCondition condition, Pageable pageable);

    Page<User> searchSignupRequests(UserSearchCondition condition, Pageable pageable, UUID hubId);
}
package com.sparta.userservice.domain.repository;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 최종 수정일: 2026-08-07
 * 설명: 사용자 정보의 조회 및 저장을 위한 데이터 접근 기능을 제공하는 Repository
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

    Optional<User> findByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    boolean existsByLoginId(String loginId);

    long countByRoleAndIsDeletedFalse(Role role);

    long countByRoleAndSignupStatusAndIsDeletedFalse(Role role, SignupStatus signupStatus);

    Optional<User> findByLoginIdAndIsDeleted(String loginId, boolean isDeleted);

    Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);

    Optional<User> findByUserIdAndIsDeletedFalse(UUID userId);
}
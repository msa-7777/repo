package com.sparta.userservice.application.port;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.User;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 최종 수정일: 2026-08-07
 * 설명: 유저 조회 기능 필요시 UserFinder Interface 주입 받아서 사용
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

public interface UserFinder {

    // 사용자 UUID로 User Entity 조회
    User getUserById(UUID userId);

    // 로그인한 사용자의 loginId로 User Entity 조회
    User getUserByLoginId(String loginId);

    // Spring Security에서 권한 검증을 처리한다면 필요 없음
    // 또는 Gateway에서 역할 검증이 된다면 중복 검사를 할 필요 없다. (혹 보안 레벨을 높이기 위해 사용할 수도 있다)
    // 사용자 조회하여 특정 역할인지 검증
    User getUserByLoginIdAndRole(String loginId, Role role);

    User getUserByLoginIdAndRoles(String loginId, Role... roles);
}
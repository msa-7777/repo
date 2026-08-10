package com.sparta.userservice.application.query;

import com.sparta.userservice.domain.model.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 설명: HTTP 요청 정보에서 페이징·정렬 값을 제외하고, 유저 DB 검색에 필요한 조건만 전달하기 위한 객체
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Getter
@RequiredArgsConstructor
public class UserSearchCondition {

    private final String loginId;

    private final String name;

    private final Role role;

    private final String keyword;

    public static UserSearchCondition of(String loginId, String name, Role role, String keyword) {
        return new UserSearchCondition(loginId, name, role, keyword);
    }
}
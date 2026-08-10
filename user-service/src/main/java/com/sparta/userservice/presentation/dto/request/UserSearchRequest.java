package com.sparta.userservice.presentation.dto.request;

import com.sparta.userservice.domain.model.Role;

import lombok.Getter;
import lombok.Setter;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 설명: 관리자 사용자 목록 검색을 위한 요청 객체
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */
@Getter
@Setter
public class UserSearchRequest {

    private String loginId;

    private String name;

    private Role role;

    private String keyword;

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "createdAt";

    private String sort = "desc";
}
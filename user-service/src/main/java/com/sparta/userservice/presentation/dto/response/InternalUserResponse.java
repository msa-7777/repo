package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-11
 * 설명: 외부 서비스가 사용자 정보를 받는 객체
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Getter
@Builder
@AllArgsConstructor
public class InternalUserResponse {

    private UUID userId;
    private String loginId;
    private String name;
    private Role role;
    private SignupStatus signupStatus;
    private String slackId;
    private UUID hubId;
    private UUID supplierId;

    public static InternalUserResponse of(User user) {
        return InternalUserResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .role(user.getRole())
                .signupStatus(user.getSignupStatus())
                .slackId(user.getSlackId())
                .hubId(user.getHubId())
                .supplierId(user.getSupplierId())
                .build();
    }
}
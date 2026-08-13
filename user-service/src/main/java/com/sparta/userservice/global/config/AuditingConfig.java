package com.sparta.userservice.global.config;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: Spring Security의 인증 정보를 기반으로 생성자 및 수정자 정보를 자동 기록하기 위한 JPA Auditing 설정 클래스
 * @EnableJpaAuditing 활성 시

userRepository.save(user)
    ↓
JPA가 엔티티 저장 준비
    ↓
 @CreatedBy 발견
    ↓
 AuditorAware 구현체를 찾음
    ↓
 AuditingConfig.getCurrentAuditor() 호출
    ↓
 현재 로그인한 loginId 반환
    ↓
 createdBy에 자동 저장
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.global.security.UserDetailsImpl;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
public class AuditingConfig implements AuditorAware<UUID> {

    private static final UUID SYSTEM_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return Optional.of(SYSTEM_USER_ID);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 현재 로그인한 사용자의 UUID 반환
        return Optional.of(userDetails.getUser().getUserId());
    }
}
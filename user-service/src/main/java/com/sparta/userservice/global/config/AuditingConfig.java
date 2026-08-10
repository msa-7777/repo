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

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditingConfig implements AuditorAware<String> {

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        // SecurityContextHolder : 현재 요청을 처리 중인 사용자 Spring Security 인증 정보를 보관하는 곳
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null // 인증 정보 X
                || !authentication.isAuthenticated() // 인증 완료 상태 확인
                || "anonymousUser".equals(authentication.getPrincipal())) { // Spring Security의 익명 사용자 확인
            return Optional.of("GUEST");
        }

        // 인증된 사용자인 경우, UserDetailsImpl 에서 Override된 함수에 따라 loginId가 반환
        return Optional.of(authentication.getName());
    }
}
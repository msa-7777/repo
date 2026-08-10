package com.sparta.userservice.global.config;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: 비밀번호 암호화 및 검증에 사용할 PasswordEncoder를 Spring Bean으로 등록하는 설정 클래스
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.sparta.productservice.global.security;

import lombok.RequiredArgsConstructor;
import org.apache.http.protocol.HTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // REST API이며 세션 기반 인증을 사용하지 않으므로 CSRF와 기본 로그인 방식을 비활성화한다.
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 인증 정보를 서버 세션에 저장하지 않는다.
                // 매 요청마다 Gateway가 전달한 X-User-Id, X-User-Role을 기준으로 인증 정보를 생성한다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL 자체의 세부 권한 판단은 여기서 하지 않는다.
                 // 역할별 접근 권한은 Controller의 @PreAuthorize에서 관리할 예정이므로 HTTP 요청 자체는 우선 허용한다.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                /// Spring Security의 기본 UsernamePasswordAuthenticationFilter보다 먼저 실행하도록 내부 헤더 인증 필터를 등록한다.
                .addFilterBefore(
                        headerAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

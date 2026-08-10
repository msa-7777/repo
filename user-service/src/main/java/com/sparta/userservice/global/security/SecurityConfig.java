package com.sparta.userservice.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.global.security.jwt.JwtAuthenticationFilter;
import com.sparta.userservice.global.security.jwt.JwtAuthorizationFilter;
import com.sparta.userservice.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: CSRF 사용 안함 / JWT 방식 사용 설정, 인증관련 접근 요청 허용 처리, 필터 순서 관리
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    private final UserDetailsServiceImpl userDetailsService;

    private final AuthenticationConfiguration authenticationConfiguration;

    private final String ContentTypeHeader = "application/json;charset=UTF-8";

    // AuthenticationManager 빈으로 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // JwtAuthenticationFilter 빈으로 등록
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        filter.setAuthenticationManager(
                authenticationManager(authenticationConfiguration)
        );
        return filter;
    }

    // JwtAuthorizationFilter 빈으로 등록
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 사용 안함
        http.csrf((csrf) ->
                csrf.disable()
        );

        // Spring Security의 session 기반 인증 상태 저장을 사용하지 않는다.
        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
        );

        // 접근 허가/불가 요청 처리
        http.authorizeHttpRequests((auth) ->
                auth
                        .requestMatchers(
                                PathRequest.toStaticResources()
                                        .atCommonLocations()
                        ).permitAll() // resources 접근 허용

                        .requestMatchers(
                                "/api/v1/auth/**"
                        )
                        .permitAll() // 'api/v1/auth/'로 시작하는 요청 모두 접근 허용 (회원가입, 로그인)

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll() // Swagger UI 허용

                        .anyRequest().authenticated() // 그 외 모든 요청 인증처리
        );

        // 필터 순서
        http.addFilterBefore(
                jwtAuthorizationFilter(),
                JwtAuthenticationFilter.class
        );
        http.addFilterBefore(
                jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class
        );

        // 인증 실패(토큰 없음)
        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling.authenticationEntryPoint(
                    (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                        response.setContentType(ContentTypeHeader);
                        response.getWriter().write(
                            new ObjectMapper().writeValueAsString(CommonResponse.fail("인증이 필요합니다."))
                        );
                })
        );

        // 지금까지의 설정을 가지고 SecurityFilterChain 객체를 만들어서 반환
        return http.build();
    }
}
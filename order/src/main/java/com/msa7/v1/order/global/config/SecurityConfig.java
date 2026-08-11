package com.msa7.v1.order.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.msa7.v1.order.global.security.HeaderAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final HeaderAuthenticationFilter headerAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/api/v1/internal/**").permitAll()
				.anyRequest().authenticated()
			);

		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)

			// 인증 정보를 서버 세션에 저장하지 않는다.
			// 매 요청마다 Gateway가 전달한 X-User-Id, X-User-Role을 기준으로 인증 정보를 생성한다.
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			);

		http
			.addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}

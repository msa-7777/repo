/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: Swagger 문서 제목 설정, JWT Bearer 인증 방식 등록, Swagger UI 전체 API에 인증 입력 기능 적용
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
package com.sparta.userservice.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "MSA-7 API", version = "v1")) // Swagger 문서 최상단에 표시되는 제목, 버전
@SecurityScheme(
        name = "bearerAuth", // 인증 설정 방식
        type = SecuritySchemeType.HTTP, // HTTP 인증 방식
        scheme = "bearer", // HTTP 요청 헤더에 Bearer 토큰 형식으로 인증 정보 전달
        bearerFormat = "JWT" // Bearer 토큰으로 JWT를 사용
)
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")); // OpenAPI 문서에 기본 인증 조건을 추가
    }
}

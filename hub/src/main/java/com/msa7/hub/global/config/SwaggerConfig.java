package com.msa7.hub.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * hub-service는 게이트웨이가 JWT 검증 후 전달하는 X-User-Id/X-User-Role 헤더로 인증한다
 * 게이트웨이를 거치지 않고 hub Swagger UI에서 직접 테스트할 때 이 두 헤더를 수동 입력할 수 있도록 등록한다.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Hub Service API", version = "v1"))
@SecurityScheme(
        name = "X-User-Id",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-User-Id",
        description = "인증된 사용자 UUID (게이트웨이가 JWT 검증 후 전달하는 값)"
)
@SecurityScheme(
        name = "X-User-Role",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-User-Role",
        description = "사용자 권한 (예: MASTER)"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-User-Id")
                        .addList("X-User-Role"));
    }
}
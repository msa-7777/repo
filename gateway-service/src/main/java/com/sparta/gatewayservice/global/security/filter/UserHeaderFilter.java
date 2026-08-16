package com.sparta.gatewayservice.global.security.filter;

import com.nimbusds.jwt.JWT;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class UserHeaderFilter implements GlobalFilter, Ordered {
    // 인증된 JWT의 사용자 정보를 내부 헤더로 변환

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 외부 사용자가 X-User-Id, X-User-Role을 직접 전달할 수 있으므로 먼저 기존 헤더를 제거한다.
        // 이후 JWT 검증을 통과한 사용자 정보만 다시 설정한다.
        ServerWebExchange sanitizedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_ROLE_HEADER);
                }))
                .build();

        // Spring Security가 JWT 인증을 완료하면 SecurityContext에 Authentication 객체가 저장된다.
        return sanitizedExchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(authentication -> {

                    /*
                     * 현재 Gateway는 OAuth2 Resource Server 방식으로 JWT를 검증하고 있으므로
                     * 정상 JWT라면 Authentication은 JwtAuthenticationToken 타입이다.
                     */
                    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                        return chain.filter(sanitizedExchange);
                    }
                    // 검증이 완료된 JWT 객체를 가져온다.
                    String userId = jwtAuthentication.getToken().getSubject();
                    String role = jwtAuthentication.getToken().getClaimAsString("role");
                    // JWT에서 검증된 사용자 정보만 내부 헤더로 추가한다.
                     /* sub  → X-User-Id
                     * role → X-User-Role
                     */
                    ServerWebExchange mutatedExchange = sanitizedExchange.mutate()
                            .request(request -> request.headers(headers -> {
                                headers.set(USER_ID_HEADER, userId);
                                headers.set(USER_ROLE_HEADER, role);
                            }))
                            .build();

                    // 수정된 요청을 실제 대상 서비스로 전달한다.
                    return chain.filter(mutatedExchange);
                })
                /*
                 * 공개 API처럼 인증 정보가 없는 요청의 경우에는
                 * 사용자 헤더 없이 그대로 Gateway 체인을 진행한다.
                 */
                .switchIfEmpty(chain.filter(sanitizedExchange));
    }

    @Override
    public int getOrder() {
        /*
         * GlobalFilter가 여러 개 있을 때 실행 순서를 결정한다.
         * 현재는 별도의 Gateway GlobalFilter가 많지 않으므로
         * 우선순위 0으로 두어도 충분하다.
         */
        return 0;
    }
}
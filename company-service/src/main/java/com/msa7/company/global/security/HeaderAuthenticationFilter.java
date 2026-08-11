package com.msa7.company.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Gateway가 JWT 검증 후 전달한 내부 헤더를 가져온다.
         /* 예)
         * X-User-Id: 550e8400-e29b-41d4-a716-446655440000
         * X-User-Role: MASTER
         */
        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);

        // 두 헤더가 모두 존재하는 경우에만 Spring Security Authentication을 생성한다.
        // Gateway를 정상적으로 거친 인증 요청이라면 두 값이 모두 존재하게 된다.
        if (userId != null && !userId.isBlank()
                && role != null && !role.isBlank()) {

            // @PreAuthorize("hasRole('MASTER')")는 내부적으로 ROLE_MASTER Authority가 있는지 확인한다.
            // 따라서 Gateway에서 전달받은 MASTER를 ROLE_MASTER 형태로 변환한다.
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            // principal에는 사용자 ID를 저장한다.
            // 이후 Controller나 Service에서 authentication.getName()을 호출하면 userId를 얻을 수 있다.
            // credentials는 이미 Gateway에서 인증이 끝났으므로 null로 둔다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(authority)
                    );

            // 생성한 Authentication을 SecurityContext에 저장한다.
            // 이후 @PreAuthorize가 이 Authentication의 Authority를이용해 권한을 검사하게 된다.
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        // 인증 정보를 설정한 뒤 다음 Security Filter 또는 Controller로 요청을 전달한다.
        filterChain.doFilter(request, response);
    }
}
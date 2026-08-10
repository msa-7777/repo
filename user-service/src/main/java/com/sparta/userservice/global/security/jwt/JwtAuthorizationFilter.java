package com.sparta.userservice.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: Authorization(허가) -> JWT를 검증하여 인증된 사용자 정보를 SecurityContext에 등록하는 역할
 * doFilterInternal() 함수 @Override 필요
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 인증 시에는 AuthenticationManager가 내부적으로 UserDetailsServiceImpl을 호출하여
    // UserDetailsImpl을 생성하고 인증 처리를 수행한다.
    //
    // JWT 인가 처리 시에는 AuthenticationManager를 거치지 않으므로,
    // JWT에서 추출한 loginId로 사용자 정보와 권한을 조회하고
    // UserDetails를 생성하여 Authentication 객체를 직접 만들기 위해 필요하다.
    private final UserDetailsServiceImpl userDetailsService;

    private final String ContentTypeHeader = "application/json;charset=UTF-8";


    /*
     * HttpServletRequest request : 클라이언트가 서버로 보낸 요청 정보
     * HttpServletResponse response : 서버가 클라이언트에게 돌려줄 응답을 만드는 객체
     * FilterChain chain : 현재 Filter 다음에 있는 Filter로 요청 처리를 계속 넘기기 위한 객체
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String tokenValue = jwtUtil.getTokenFromRequest(request); // JWT 추출

        if (StringUtils.hasText(tokenValue)) { // 존재하는 경우 검증 진행
            tokenValue = jwtUtil.subStringToken(tokenValue);

            if (jwtUtil.validateToken(tokenValue)) { // JWT 토큰 검증
                Claims info = jwtUtil.getUserInfoFromToken(tokenValue); // JWT payload에서 사용자 정보 추출

                String loginId = info.get(JwtUtil.USERNAME_KEY, String.class);

                try {
                    setAuthentication(loginId); // JwtUtil에서 subject에 loginId를 넣어놨었음
                } catch (UsernameNotFoundException e) {
                    log.warn("Authentication Failed: {}", e.getMessage());
                    writeUnauthorized(response, "인증에 실패했습니다.");
                    return;

                } catch (Exception e) {
                    log.error("Authentication Error: {}", e.getMessage(), e);
                    writeUnauthorized(response, "인증에 실패했습니다.");
                    return;
                }
            }
            else {
                log.warn("Token Error");
                writeUnauthorized(response, "유효하지 않은 토큰입니다.");
                return ;
            }
        }

        // JWT가 없거나, JWT 인증 처리가 정상적으로 완료된 경우 다음 Filter로 요청 전달
        filterChain.doFilter(request, response);
    }

    // JSON 응답
    private void writeUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(ContentTypeHeader);
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        CommonResponse.fail(message)
                )
        );
    }

    // 아래 함수 성공 시 JWT에서 알아낸 사용자를 Spring Security에게 인증된 요청자라고 등록
    private void setAuthentication(String loginId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(loginId);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String loginId) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}

package com.sparta.userservice.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.global.security.UserDetailsImpl;
import com.sparta.userservice.presentation.dto.request.UserLoginRequest;
import com.sparta.userservice.presentation.dto.response.UserLoginResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: Authentication(인증) -> 로그인 시도, 로그인 성공 시 JWT 생성, 로그인 실패 처리
 * UsernamePasswordAuthenticationFilter : Spring Security에서 아이디/비밀번호 기반 로그인을 처리하는 기본 필터 클래스
 * 이를 상속받아 구현

 POST /api/v1/auth/login
    ↓
 JwtAuthenticationFilter
    ↓
 loginId / password 인증
    ↓
 User 조회
    ↓
 Role 확인
    ↓
 JWT 생성
 ┌──────────────┐
 │ sub: master01│
 │ auth: MASTER │
 └──────────────┘

 * HttpServletRequest와 HttpServletResponse는 우리가 만드는 객체가 아니라,
 * 웹 서버(Servlet Container)가 요청 하나가 들어올 때 미리 만들어서 Filter에게 넘겨주는 객체
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

// 여기서 구현하는 메서드는 UsernamePasswordAuthenticationFilter 구조를 따라야 한다.
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;

    private final String ContentTypeHeader = "application/json;charset=UTF-8";

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/v1/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.info("로그인 시도");
        try {
            // Http 요청 body의 JSON을 객체로 변환
            UserLoginRequest requestDto =new ObjectMapper().
                    readValue(
                            request.getInputStream(),
                            UserLoginRequest.class
                    );

            // loginId/password 조합이 유효한 지에 대한 판단을 AuthenticationManager에게 위임한다.
            // role 은 Id-password 검증에서 무의미 하기에 null 입력
            return getAuthenticationManager().authenticate(
                        new UsernamePasswordAuthenticationToken(
                            requestDto.getLoginId(),
                            requestDto.getPassword(),
                            null
                        )
                    );
        } catch (IOException e) {
            String message = "로그인 요청 데이터 읽기 실패: " + e.getMessage();
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    /*
     * HttpServletRequest request : 클라이언트가 서버로 보낸 요청 정보
     * HttpServletResponse response : 서버가 클라이언트에게 돌려줄 응답을 만드는 객체
     * FilterChain chain : 현재 Filter 다음에 있는 Filter로 요청 처리를 계속 넘기기 위한 객체
     * Authentication authResult : Spring Security 인증에 성공한 결과 객체
     */
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");
        User user = ((UserDetailsImpl) authResult.getPrincipal()).getUser(); // 인증된 사용자 정보를 User Entity로 변환

        String token = jwtUtil.createToken(user.getUserId(), user.getLoginId(), user.getRole());
        jwtUtil.addJwtToCookie(token, response); // 로그인 성공하면 cookie에 jwt를 추가한다.

        UserLoginResponse loginResponse = UserLoginResponse.of(user, token); // JSON 응답을 위해 생성한다.

        // 로그인 성공 결과를 Http 응답으로 생성
        response.setStatus(HttpServletResponse.SC_OK); // 200
        response.setContentType(ContentTypeHeader);
        response.getWriter().write(
                new ObjectMapper()
                        .writeValueAsString(
                                CommonResponse.success("로그인에 성공했습니다.", loginResponse)
                        )
        );
    }

    /*
     * HttpServletRequest request : 클라이언트가 서버로 보낸 요청 정보
     * HttpServletResponse response : 서버가 클라이언트에게 돌려줄 응답을 만드는 객체
     * AuthenticationException failed : Spring Security 인증 실패 시 발생한 예외 객체
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        log.info("로그인 실패");

        // 로그인
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(ContentTypeHeader);
        response.getWriter().write(
                new ObjectMapper()
                        .writeValueAsString(
                                CommonResponse.fail("아이디 또는 비밀번호가 일치하지 않습니다.")
                        )
        );
    }
}

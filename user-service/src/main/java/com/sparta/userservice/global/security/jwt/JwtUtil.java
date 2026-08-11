package com.sparta.userservice.global.security.jwt;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: JWT 토큰 생성 / JWT를 Cookie 저장 / Token 검증 및 parsing / HttpServletRequest에서 Header or Cookie 영역 중에 JWT 가져오기
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.exception.CommonErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    // Header Key
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String USERNAME_KEY = "username";

    public static final String ROLE_KEY = "role";

    // Token 식별자
    public static final String BEARER = "Bearer ";
    // Access Token 만료 시간 - 60분
    private final long TOKEN_TIME = 60 * 60 * 1000L;

    @Value("${SECURITY_JWT_SECRET}")
    private String secretKey;

    private SecretKey key; // 디코딩해서 담을 키

    // .signWith(key) 를 사용하게 되면 JJWT가 적절한 HMAC 알고리즘을 선택하여 사용한다. 특정 알고리즘으로 지정하려면 아래와 같이 사용한다.
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // jwt키 decode 해서 Secret key 객체로 초기화
    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Token 생성
    public String createToken(UUID userId, String loginId, Role role) {
        Date now = new Date();
        return BEARER +
                Jwts.builder()
                        .subject(userId.toString())               // sub = 사용자 UUID
                        .claim(USERNAME_KEY, loginId)             // username = user01
                        .claim(ROLE_KEY, role.name())             // role = MASTER
                        .issuedAt(now)                            // iat
                        .expiration(new Date(now.getTime() + TOKEN_TIME)) // exp
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    // 생성된 JWT를 Cookie에 저장
    public void addJwtToCookie(String token, HttpServletResponse response) {
        token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
        cookie.setPath("/");

        // Response 객체에 Cookie 추가
        response.addCookie(cookie);
    }

    // JWT 정보를 담은 Cookie 삭제
    public void deleteJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, null); // [이름: Authorization, 값: null] 쿠키 생성

        cookie.setMaxAge(0); // 쿠키를 즉시 만료시키는 설정
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 브라우저의 JavaScript에서 그 쿠키를 읽지 못하게 하는 보안 설정

        response.addCookie(cookie); // 브라우저가 같은 쿠키라고 판단하면 새로운 쿠키로 교체한다. 쿠키는 만료하면 삭제된다.
    }

    // JWT에서 "Bearer "를 잘라내고 순수 JWT 문자열만 반환
    public String subStringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER)) {
            return tokenValue.substring(BEARER.length());
        }

        log.warn("JWT Token이 존재하지 않거나 Bearer 형식이 아닙니다.");
        throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }

    // JWT 토큰 검증 (위변조, 만료, 형식 검증)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        }
        catch (SecurityException e) { log.warn("유효하지 않는 JWT 서명입니다."); }
        catch (MalformedJwtException e) { log.warn("잘못된 형식의 JWT token 입니다."); }
        catch (ExpiredJwtException e) { log.warn("만료된 JWT token 입니다."); }
        catch (UnsupportedJwtException e) { log.warn("지원하지 않는 JWT 토큰입니다."); }
        catch (IllegalArgumentException e) { log.warn("JWT token이 비어있거나 잘못되었습니다."); }
        return false;
    }

    // Token을 parsing 해서 Header.Payload.Signature 중 Payload 부분을 반환
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // HttpServletRequest 에서 Cookie Value (JWT 가져오기)
    public String getTokenFromRequest(HttpServletRequest request) {
        // 1. Authorization Header 확인, 있으면 반환
        String headerValue = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerValue)) {
            return headerValue; // 헤더 값은 인코딩되지 않는다
        }

        // 2. Cookie 확인
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (AUTHORIZATION_HEADER.equals(cookie.getName())) {
                return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}

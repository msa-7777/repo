package com.sparta.userservice.global.security.jwt;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: 프로젝트에 사용할 단순 JWT 키를 생성 및 출력
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtKeyGenerator {

    public static void main(String[] args) {

        SecretKey key = Jwts.SIG.HS256.key().build();

        String secretKey = Base64.getEncoder()
                .encodeToString(key.getEncoded());

        System.out.println(secretKey);
    }
}

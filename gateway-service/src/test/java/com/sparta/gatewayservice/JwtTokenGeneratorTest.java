package com.sparta.gatewayservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

class JwtTokenGeneratorTest {

    @Test
    void generateToken() {
        String secret = System.getenv("JWT_SECRET");
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                /*.subject(UUID.randomUUID().toString())*/
                .subject("550e8400-e29b-41d4-a716-446655440000")
                .claim("username", "user01")
                .claim("role", "COMPANY_MANAGER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        System.out.println(token);
    }
}

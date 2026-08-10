package com.sparta.userservice.global.security;

import com.sparta.userservice.domain.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: Spring Security가 사용할 클래스는 UserDetails 이다.
 * 우리는 UserDetails를 상속한 클래스를 통해 User의 정보를 가져올 수 있다. (일종의 이 클래스는 adapter 역할이다)
 * - getPassword(), getUsername(), getAuthorities()를 Override 해야 한다.
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(
                new SimpleGrantedAuthority(
                        user.getRole().getAuthority()
                )
        );

        return authorities;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getLoginId(); } // loginId로 식별 (동명이인이 있기에 name은 부적합)
}

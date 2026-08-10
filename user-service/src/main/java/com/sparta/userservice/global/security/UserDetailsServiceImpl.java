package com.sparta.userservice.global.security;

import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-09
 * 설명: Spring Security가 로그인 아이디로 사용자를 조회할 때 쓰는 adapter 역할
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByLoginIdAndIsDeletedFalse(loginId);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("Not found: " + loginId);
        }

        return new UserDetailsImpl(optionalUser.get());
    }
}

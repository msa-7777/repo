package com.sparta.userservice.application.service;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: login(), signup() 구현
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.exception.CommonErrorCode;
import com.sparta.userservice.global.security.jwt.JwtUtil;
import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.presentation.dto.request.UserLoginRequest;
import com.sparta.userservice.presentation.dto.request.UserSignupRequest;
import com.sparta.userservice.presentation.dto.response.UserLoginResponse;
import com.sparta.userservice.presentation.dto.response.UserSignupResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class UserAuthService {

    private final UserFinder userFinder;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    // 로그인 - 성공 시 jwt 발급 후 cookie에 저장 { loginId, password }
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest userLogin) {
        User user = userFinder.getUserByLoginId(userLogin.getLoginId());

        if (!passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_LOGIN_INFO);
        }

        String accessToken = jwtUtil.createToken(user.getUserId(), user.getLoginId(), user.getRole());

        return UserLoginResponse.of(user, accessToken); // { accessToken, userId, loginId, name, role }
    }

    // 회원가입 - { loginId, password, name, phone, email, slackId, role, [ hubId, supplierId ] }
    @Transactional(rollbackFor = Exception.class)
    public UserSignupResponse signup(UserSignupRequest signupUser) {
        // 로그인 아이디 확인 (unique)
        if (userRepository.existsByLoginId(signupUser.getLoginId())) {
            log.warn("SIGNUP_FAILED: Duplicate loginId = {}", signupUser.getLoginId());

            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 이메일 확인 (unique)
        if (userRepository.existsByEmail(signupUser.getEmail())) {
            log.warn("SIGNUP_FAILED: Duplicate email = {}", signupUser.getEmail());

            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 정책 상 MASTER는 일반적으로 생성될 수 없다.
        if (signupUser.getRole() == Role.MASTER) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        User user = User.createBySignup(signupUser, passwordEncoder); // 객체 생성

        User savedUser = userRepository.save(user); // DB 저장

        log.info("SIGNUP_SUCCESS: loginId = {}, role = {}", savedUser.getLoginId(), savedUser.getRole());

        return UserSignupResponse.of(savedUser); // { userId, loginId, name, email, role, signupStatus }
    }
}

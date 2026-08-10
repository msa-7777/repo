package com.sparta.userservice.application.service;

import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-07
 * 설명: 사용자 정보 수정 시 중복 여부를 검증하고 User의 정보 변경을 처리하는 Service
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Service
@RequiredArgsConstructor
@Transactional // BusinessException이 RuntimeException을 상속받기에 Transactional 설정하면 Exception 발생 시 롤백된다.
public class UserInfoUpdateService {

    private final UserRepository userRepository;

    void changeLoginId(User user, String newLoginId) {
        if (isNullOrBlank(newLoginId))
            return ;
        if (user.getLoginId().equals(newLoginId))
            return ;
        if (userRepository.existsByLoginId(newLoginId))
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);

        user.changeLoginId(newLoginId);
    }

    void changePassword(User user, String newPassword, PasswordEncoder passwordEncoder) {
        if (isNullOrBlank(newPassword))
            return ;
        if (passwordEncoder.matches(newPassword, user.getPassword()))
            return ;

        user.changePassword(newPassword, passwordEncoder); // User Entity에서 저장할 때 newPassword를 encode 해서 저장함
    }

    void changeName(User user, String newName) {
        if (isNullOrBlank(newName))
            return ;
        if (user.getName().equals(newName))
            return ;

        user.changeName(newName);
    }

    void changeEmail(User user, String newEmail) {
        if (isNullOrBlank(newEmail))
            return ;
        if (user.getEmail().equals(newEmail))
            return ;
        if (userRepository.existsByEmail(newEmail))
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        // TODO: 이메일 인증을 받는 로직 (추후에 확장)

        user.changeEmail(newEmail);
    }

    void changePhone(User user, String newPhone) {
        if (isNullOrBlank(newPhone))
            return ;
        if (user.getPhone().equals(newPhone))
            return ;
        // TODO: 전화번호 인증을 받는 로직 (추후에 확장)
        // 여러 개의 핸드폰을 등록할 수 있도록 함.

        user.changePhone(newPhone);
    }

    void changeRole(User user, Role newRole) {
        if (newRole == null)
            return ;
        if (user.getRole() == newRole)
            return ;

        user.changeRole(newRole);
    }

    void changeSignupStatus(User user, SignupStatus newSignupStatus) {
        if (newSignupStatus == null)
            return ;
        if (user.getSignupStatus() == newSignupStatus)
            return ;

        user.changeSignupStatus(newSignupStatus);
    }

    void changeSlackId(User user, String newSlackId) {
        if (isNullOrBlank(newSlackId))
            return ;
        if (user.getSlackId().equals(newSlackId))
            return ;
        // TODO: newSlackId 가 실제 slack에 연동이 되는 아이디인지 확인하는 로직 (추후에 확장)
        // 여러 개의 slack id를 등록할 수 있도록 함.

        user.changeSlackId(newSlackId);
    }

    void changeHubId(User user, UUID newHubId) {
        if (newHubId == null)
            return ;
        if (Objects.equals(user.getHubId(), newHubId))
            return ;

        user.changeHubId(newHubId);
    }

    void changeSupplierId(User user, UUID newSupplierId) {
        if (newSupplierId == null)
            return ;
        if (Objects.equals(user.getSupplierId(), newSupplierId))
            return ;

        user.changeSupplierId(newSupplierId);
    }

    private boolean isNullOrBlank(String s) { return s == null || s.isBlank(); }
}

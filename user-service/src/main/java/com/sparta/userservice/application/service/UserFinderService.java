package com.sparta.userservice.application.service;

import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-07
 * 설명: 사용자 식별 정보를 기반으로 사용자를 조회하고, 필요한 경우 역할 권한을 검증하는 서비스
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 서비스의 메서드들이 기본적으로 조회 전용 Transaction으로 동작, 데이터 변경 목적 X
public class UserFinderService implements UserFinder {

    private final UserRepository userRepository;

    @Override
    public User getUserById(UUID userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException((UserErrorCode.NOT_FOUND_USER)));
    }

    @Override
    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));
    }

    @Override
    public User getUserByLoginIdAndRole(String loginId, Role role) {
        User user = getUserByLoginId(loginId);

        if (user.getRole() != role) {
            // TODO: 찾은 user에 대한 role에 대해 권한 허용이 되는 role이 아닐 시
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
        return user;
    }

    @Override
    public User getUserByLoginIdAndRoles(String loginId, Role... roles) {
        User user = getUserByLoginId(loginId);

        Role userRole = user.getRole();
        boolean hasRole = false;

        for (Role role : roles) {
            if (userRole == role) {
                hasRole = true;
                break;
            }
        }

        if (!hasRole) {
            // TODO: 찾은 user에 대한 role에 대해 권한 허용이 되는 role이 아닐 시
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
        return user;
    }
}

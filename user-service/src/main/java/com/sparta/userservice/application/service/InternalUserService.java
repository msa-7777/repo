package com.sparta.userservice.application.service;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-11
 * 설명: 외부 서비스가 사용자 정보를 사용하기 위한 서비스
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.presentation.dto.response.InternalUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalUserService {

    private final UserFinder userFinder;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public InternalUserResponse getUser(UUID userId) {

        User user = userFinder.getUserById(userId);

        return InternalUserResponse.of(user);
    }

    @Transactional(readOnly = true)
    public boolean existsUserByHubId(UUID hubId) {

        return userRepository.existsByHubIdAndIsDeletedFalse(hubId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getDeliveryManagerIds(UUID hubId) {

        List<User> users = userRepository.findAllByHubIdAndRoleAndIsDeletedFalse(hubId, Role.DELIVERY_AGENT);

        if (users.isEmpty()) {
            throw new BusinessException(UserErrorCode.NOT_FOUND_DELIVERY_MANAGER);
        }

        return users.stream()
                .map(User::getUserId)
                .toList();
    }
}
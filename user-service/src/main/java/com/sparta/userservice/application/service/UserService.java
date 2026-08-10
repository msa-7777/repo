package com.sparta.userservice.application.service;

import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.exception.CommonErrorCode;
import com.sparta.userservice.presentation.dto.request.UserDeleteRequest;
import com.sparta.userservice.presentation.dto.request.UserUpdateRequest;
import com.sparta.userservice.presentation.dto.response.UserDeleteResponse;
import com.sparta.userservice.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: getMyInfo(), updateMyInfo(), deleteMyAccount() 구현
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final UserFinder userFinder;

    private final UserInfoUpdateService userInfoUpdateService;


    // 자신의 회원정보 조회
    // UserFiderService에서 User Entity -> UserService에서 해당 Response Object
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(String loginId) {
        User user = userFinder.getUserByLoginId(loginId);

        return UserInfoResponse.of(user);
    }

    // 자신의 정보 수정 - password, name, phone, email, slackId 가능
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateMyInfo(String loginId, UserUpdateRequest updateUser) {
        User user = userFinder.getUserByLoginId(loginId);

        List<String> updateFields = updateUser.getUpdateFields();

        for (String field : updateFields) {
            switch (field) {
                case "password" -> userInfoUpdateService.changePassword(user, updateUser.getPassword(), passwordEncoder);
                case "name" -> userInfoUpdateService.changeName(user, updateUser.getName());
                case "phone" -> userInfoUpdateService.changePhone(user, updateUser.getPhone());
                case "email" -> userInfoUpdateService.changeEmail(user, updateUser.getEmail());
                case "slackId" -> userInfoUpdateService.changeSlackId(user, updateUser.getSlackId());

                default -> throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE); // 잘못된 입력값으로 아무것도 매칭 안됨
            }
        }

        log.info("USER_UPDATE_SUCCESS: loginId = {}, updatedFields = {}", user.getLoginId(), updateFields);

        return UserInfoResponse.of(user);
    }

    // 자신의 회원 탈퇴
    @Transactional(rollbackFor = Exception.class)
    public UserDeleteResponse deleteMyAccount(String loginId, UserDeleteRequest deleteUser) {
        User user = userFinder.getUserByLoginId(loginId);

        // 회원 탈퇴를 위한 인증 정보 검증
        if (!user.verifyCredentialsForDelete(deleteUser, passwordEncoder)) {
            log.warn("인증 정보 불일치: loginId={}", user.getLoginId());
            throw new BusinessException(UserErrorCode.INVALID_DELETE_CREDENTIALS);
        }

        // MASTER인 경우 마지막 MASTER인지 확인
        if (user.getRole() == Role.MASTER) {
            long masterUserCount = userRepository.countByRoleAndSignupStatusAndIsDeletedFalse(Role.MASTER, SignupStatus.APPROVED);

            log.info("MASTER 권한 탈퇴 요청 검증 시작: masterUserCount={}", masterUserCount);

            if (masterUserCount <= 1) {
                throw new BusinessException(UserErrorCode.DELETE_FAILURE_LAST_MASTER);
            }
        }

        // Soft Delete 처리
        user.softDelete(user.getLoginId());

        log.info("USER_DELETE_SUCCESS: loginId={}", user.getLoginId());

        return UserDeleteResponse.of(user);
    }
}

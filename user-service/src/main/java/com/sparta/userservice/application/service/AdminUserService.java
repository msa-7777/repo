package com.sparta.userservice.application.service;

import com.sparta.userservice.Infrastructure.persistence.querydsl.UserRepositoryImpl;
import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.application.query.UserSearchCondition;
import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.exception.CommonErrorCode;
import com.sparta.userservice.global.response.PageResponse;
import com.sparta.userservice.global.util.PageUtil;
import com.sparta.userservice.presentation.dto.request.AdminUserUpdateRequest;
import com.sparta.userservice.presentation.dto.request.UserSearchRequest;
import com.sparta.userservice.presentation.dto.request.UserSignupRequest;
import com.sparta.userservice.presentation.dto.response.UserDeleteResponse;
import com.sparta.userservice.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: createUser(), getUser(), searchUsers(), updateUser(), deleteUser() 구현

 * 페이징 동작 방식
 AdminUserController
         ↓
 UserSearchRequest 수신
         ↓
 AdminUserService.searchUsers(userSearch)
         ↓
 UserSearchCondition 생성
    + Pageable 생성
         ↓
 UserRepository.searchApprovedUsers(...)
         ↓
 UserRepositoryImpl.searchApprovedUsers(...)  // Querydsl 실제 구현
         ↓
 Page<User>
         ↓
 PageResponse.from(
    users,
    UserInfoResponse::of
 )
         ↓
 PageResponse<UserInfoResponse>
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {

    private final UserFinder userFinder;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserInfoUpdateService userInfoUpdateService;

    // 관리자가 회원 생성
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse createUser(UserSignupRequest createUser) {

        // loginId 중복 검사
        if (userRepository.existsByLoginId(createUser.getLoginId())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }

        // email 중복 검사
        if (userRepository.existsByEmail(createUser.getEmail())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.createByAdmin(createUser, passwordEncoder);

        userRepository.save(user);

        return UserInfoResponse.of(user);
    }

    // 개별 회원 조회
    @Transactional(readOnly = true)
    public UserInfoResponse getUser(UUID userId) {
        User user = userFinder.getUserById(userId);

        return UserInfoResponse.of(user);
    }

    // 회원 목록 조회 및 검색 (Querydsl 사용)
    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> searchUsers(UserSearchRequest userSearch) {

        // Repository 검색에 사용할 조건 객체로 변환
        UserSearchCondition condition = UserSearchCondition.of(
                userSearch.getLoginId(),
                userSearch.getName(),
                userSearch.getRole(),
                userSearch.getKeyword()
        );

        // 공통 페이징 정책 적용
        Pageable pageable = PageUtil.toPageable(
                userSearch.getPage(),
                userSearch.getSize(),
                userSearch.getSort()
        );

        // Querydsl 사용해 검색 조건과 페이징 조건에 맞고, 삭제되지 않는 회원 목록 조회
        Page<User> users = userRepository.searchApprovedUsers(
                condition,
                pageable
        );

        // 조회된 User Entity 목록을 UserInfoResponse로 변환해서 반환
        return PageResponse.from(
                users,
                UserInfoResponse::of
        );
    }


    // 회원 정보 수정 - loginId, password, name, phone, email, slackId, role, signupStatus, hubId, supplierId 수정 가능
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateUser(UUID userId, AdminUserUpdateRequest updateUser) {
        User user = userFinder.getUserById(userId);

        List<String> updateFields = updateUser.getUpdateFields();

        for (String field : updateFields) {
            switch (field) {
                case "loginId" -> userInfoUpdateService.changeLoginId(user, updateUser.getLoginId());
                case "password" -> userInfoUpdateService.changePassword(user, updateUser.getPassword(), passwordEncoder);
                case "name" -> userInfoUpdateService.changeName(user, updateUser.getName());
                case "email" -> userInfoUpdateService.changeEmail(user, updateUser.getEmail());
                case "phone" -> userInfoUpdateService.changePhone(user, updateUser.getPhone());
                case "role" -> userInfoUpdateService.changeRole(user, updateUser.getRole());
                case "signupStatus" -> userInfoUpdateService.changeSignupStatus(user, updateUser.getSignupStatus());
                case "slackId" -> userInfoUpdateService.changeSlackId(user, updateUser.getSlackId());
                case "hubId" -> userInfoUpdateService.changeHubId(user, updateUser.getHubId());
                case "supplierId" -> userInfoUpdateService.changeSupplierId(user, updateUser.getSupplierId());

                default -> throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
            }
        }

        log.info("ADMIN_USER_UPDATE_SUCCESS: loginId = {}, updatedFields = {}", user.getLoginId(), updateFields);

        return UserInfoResponse.of(user);
    }

    // Master의 회원 삭제 - loginId로
    @Transactional(rollbackFor = Exception.class)
    public UserDeleteResponse deleteUser(UUID userId, String loginId) {
        User user = userFinder.getUserById(userId);

        // 마지막 MASTER 계정은 삭제 불가
        if (user.getRole() == Role.MASTER
                && userRepository.countByRoleAndSignupStatusAndIsDeletedFalse(Role.MASTER, SignupStatus.APPROVED) <= 1) {
            throw new BusinessException(UserErrorCode.DELETE_FAILURE_LAST_MASTER);
        }

        user.softDelete(userId);

        log.info("ADMIN_USER_DELETE_SUCCESS: userId = {}, loginId = {}, deletedBy = {}", user.getUserId(), user.getLoginId(), loginId);

        return UserDeleteResponse.of(user);
    }
}

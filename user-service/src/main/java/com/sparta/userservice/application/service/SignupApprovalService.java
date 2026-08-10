package com.sparta.userservice.application.service;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: getSignupRequest(), searchSignupRequests(), approve(), reject() 구현
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.application.query.UserSearchCondition;
import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.domain.repository.UserRepository;
import com.sparta.userservice.global.exception.BusinessException;
import com.sparta.userservice.global.response.PageResponse;
import com.sparta.userservice.global.util.PageUtil;
import com.sparta.userservice.presentation.dto.request.UserSearchRequest;
import com.sparta.userservice.presentation.dto.response.SignupRequestResponse;
import com.sparta.userservice.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupApprovalService {

    private final UserFinder userFinder;

    private final UserRepository userRepository;

    // 개별 회원가입 요청 조회
    @Transactional(readOnly = true)
    public SignupRequestResponse getSignupRequest(UUID userId) {

        User user = userFinder.getUserById(userId);

        if (user.getSignupStatus() != SignupStatus.PENDING) {
            throw new BusinessException(UserErrorCode.NOT_FOUND_SIGNUP_REQUEST);
        }

        return SignupRequestResponse.of(user);
    }

    // 회원가입 요청 목록 조회 및 검색 (Querydsl 사용)
    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> searchSignupRequests(String loginId, UserSearchRequest userSearch) {

        // 회원가입 요청을 조회하는 사용자 조회
        User approver = userFinder.getUserByLoginId(loginId);

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

        // MASTER는 전체 조회, HUB_MANAGER는 자신의 허브에 해당하는 요청만 조회
        UUID hubId = null;

        if (approver.getRole() == Role.HUB_MANAGER) {
            hubId = approver.getHubId();
        }

        // Querydsl을 사용해 검색 조건과 페이징 조건에 맞는
        // 승인 대기 중인 회원가입 요청 목록 조회
        Page<User> users = userRepository.searchSignupRequests(
                condition,
                pageable,
                hubId
        );

        // 조회된 User Entity 목록을 UserInfoResponse로 변환해서 반환
        return PageResponse.from(
                users,
                UserInfoResponse::of
        );
    }

    // 회원가입 승인
    @Transactional(rollbackFor = Exception.class)
    public SignupRequestResponse approve(String loginId, UUID userId) {

        User targetUser = validateSignupRequest(loginId, userId);

        targetUser.approveSignup();

        return SignupRequestResponse.of(targetUser);
    }


    // 회원가입 요청 거절
    @Transactional(rollbackFor = Exception.class)
    public SignupRequestResponse reject(String loginId, UUID userId) {

        User targetUser = validateSignupRequest(loginId, userId);

        targetUser.rejectSignup();

        return SignupRequestResponse.of(targetUser);
    }

    // 회원가입 요청 승인/거절 권한 검증
    private User validateSignupRequest(String loginId, UUID userId) {

        User approver = userFinder.getUserByLoginId(loginId);
        User targetUser = userFinder.getUserById(userId);

        // 승인 대기 상태가 아닌 경우
        if (targetUser.getSignupStatus() != SignupStatus.PENDING) {
            throw new BusinessException(UserErrorCode.SIGNUP_REQUEST_NOT_PENDING);
        }

        // 허브 관리자는 동일 허브 소속의 회원가입 요청만 처리 가능
        if (approver.getRole() == Role.HUB_MANAGER &&
                !Objects.equals(approver.getHubId(), targetUser.getHubId())) {
            throw new BusinessException(UserErrorCode.DIFFERENT_HUB_ACCESS_DENIED);
        }

        return targetUser;
    }
}

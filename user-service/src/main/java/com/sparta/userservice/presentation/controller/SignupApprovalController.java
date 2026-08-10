package com.sparta.userservice.presentation.controller;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: 회원가입 요청 개별 조회, 회원가입 요청 목록 조회, 승인, 거절
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.service.SignupApprovalService;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.global.response.PageResponse;
import com.sparta.userservice.presentation.dto.request.UserSearchRequest;
import com.sparta.userservice.presentation.dto.response.SignupRequestResponse;
import com.sparta.userservice.presentation.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/signup-requests")
@Tag(name = "Signup Approval", description = "회원가입 요청 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class SignupApprovalController {

    private final SignupApprovalService signupApprovalService;

    // 1-10-1 개별 회원가입 요청 조회
    @Operation(
            summary = "개별 회원가입 요청 조회",
            description = "특정 회원가입 요청을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 요청 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원가입 요청")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<SignupRequestResponse>> getSignupRequest(
            @PathVariable UUID userId
    ) {
        SignupRequestResponse response =
                signupApprovalService.getSignupRequest(userId);

        return ResponseEntity.ok(
                CommonResponse.success("회원가입 요청 조회 성공", response)
        );
    }


    // 1-10-2 모든 회원가입 요청 목록 조회
    @Operation(
            summary = "모든 회원가입 요청 목록 조회",
            description = "승인 대기 중인 회원가입 요청 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 요청 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "조회 조건 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<UserInfoResponse>>> getSignupRequests(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute UserSearchRequest request
    ) {
        String loginId = userDetails.getUsername();

        PageResponse<UserInfoResponse> response =
                signupApprovalService.searchSignupRequests(loginId, request);

        return ResponseEntity.ok(
                CommonResponse.success("회원가입 요청 목록 조회 성공", response)
        );
    }


    // 1-11 회원가입 승인
    @Operation(
            summary = "회원가입 승인",
            description = "회원가입 요청을 승인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 승인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원가입 요청"),
            @ApiResponse(responseCode = "409", description = "승인 대기 상태가 아닌 요청")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'ROLE_HUB_MANAGER')")
    @PostMapping("/{userId}/approve")
    public ResponseEntity<CommonResponse<SignupRequestResponse>> approve(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId
    ) {
        String loginId = userDetails.getUsername();

        SignupRequestResponse response =
                signupApprovalService.approve(loginId, userId);

        return ResponseEntity.ok(
                CommonResponse.success("회원가입 승인 성공", response)
        );
    }


    // 1-12 회원가입 거절
    @Operation(
            summary = "회원가입 거절",
            description = "회원가입 요청을 거절합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 거절 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원가입 요청"),
            @ApiResponse(responseCode = "409", description = "승인 대기 상태가 아닌 요청")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'ROLE_HUB_MANAGER')")
    @PostMapping("/{userId}/reject")
    public ResponseEntity<CommonResponse<SignupRequestResponse>> reject(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId
    ) {
        String loginId = userDetails.getUsername();

        SignupRequestResponse response =
                signupApprovalService.reject(loginId, userId);

        return ResponseEntity.ok(
                CommonResponse.success("회원가입 거절 성공", response)
        );
    }
}
package com.sparta.userservice.presentation.controller;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: MASTER가 회원 생성, 개별 회원 조회, 회원 목록 조회, 회원 정보 수정, 회원 삭제
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.service.AdminUserService;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.global.response.PageResponse;
import com.sparta.userservice.presentation.dto.request.AdminUserUpdateRequest;
import com.sparta.userservice.presentation.dto.request.UserSearchRequest;
import com.sparta.userservice.presentation.dto.request.UserSignupRequest;
import com.sparta.userservice.presentation.dto.response.UserDeleteResponse;
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
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin User", description = "관리자 회원 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 1-6 회원 생성
    @Operation(
            summary = "회원 생성",
            description = "MASTER 권한으로 새로운 회원을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 회원 정보")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @PostMapping
    public ResponseEntity<CommonResponse<UserInfoResponse>> createUser(
            @Valid @RequestBody UserSignupRequest request
    ) {
        UserInfoResponse response = adminUserService.createUser(request);

        return ResponseEntity.ok(
                CommonResponse.success("회원 생성 성공", response)
        );
    }

    // 1-7-1 개별 회원 조회
    @Operation(
            summary = "개별 회원 조회",
            description = "MASTER 권한으로 특정 회원의 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "개별 회원 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserInfoResponse>> getUser(
            @PathVariable UUID userId
    ) {
        UserInfoResponse response = adminUserService.getUser(userId);

        return ResponseEntity.ok(
                CommonResponse.success("개별 회원 조회 성공", response)
        );
    }

    // 1-7-2 모든 회원 목록 조회
    @Operation(
            summary = "모든 회원 목록 조회",
            description = "MASTER 권한으로 회원 목록을 검색 조건 및 페이지 단위로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "조회 조건 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<UserInfoResponse>>> getUsers(
            @Valid @ModelAttribute UserSearchRequest request
    ) {
        PageResponse<UserInfoResponse> response =
                adminUserService.searchUsers(request);

        return ResponseEntity.ok(
                CommonResponse.success("회원 목록 조회 성공", response)
        );
    }

    // 1-8 회원 정보 수정
    @Operation(
            summary = "회원 정보 수정",
            description = "MASTER 권한으로 특정 회원의 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "409", description = "중복되거나 기존과 동일한 회원 정보")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @PatchMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserInfoResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        UserInfoResponse response =
                adminUserService.updateUser(userId, request);

        return ResponseEntity.ok(
                CommonResponse.success("회원 정보 수정 성공", response)
        );
    }

    // 1-9 회원 삭제(Soft delete)
    @Operation(
            summary = "회원 삭제",
            description = "MASTER 권한으로 특정 회원을 Soft Delete 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "409", description = "회원 삭제 불가")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserDeleteResponse>> deleteUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId
    ) {
        String loginId = userDetails.getUsername();

        UserDeleteResponse response =
                adminUserService.deleteUser(userId, loginId);

        return ResponseEntity.ok(
                CommonResponse.success("회원 삭제 성공", response)
        );
    }
}
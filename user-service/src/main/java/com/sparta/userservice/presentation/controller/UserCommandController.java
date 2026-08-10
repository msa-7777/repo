package com.sparta.userservice.presentation.controller;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: 내 정보 수정, 내 회원 탈퇴
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.service.UserService;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.presentation.dto.request.UserDeleteRequest;
import com.sparta.userservice.presentation.dto.request.UserUpdateRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "회원 Command API")
@SecurityRequirement(name = "bearerAuth")
public class UserCommandController {

    private final UserService userService;

    // 1-4 내 정보 수정
    @Operation(
            summary = "내 정보 수정",
            description = "로그인한 사용자의 회원 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "409", description = "중복되거나 기존과 동일한 회원 정보")
    })
    @PatchMapping("/me")
    public ResponseEntity<CommonResponse<UserInfoResponse>> updateMyInfo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        String loginId = userDetails.getUsername();

        UserInfoResponse response =
                userService.updateMyInfo(loginId, request);

        return ResponseEntity.ok(
                CommonResponse.success("내 정보 수정 성공", response)
        );
    }

    // 1-5 회원 탈퇴(Soft delete)
    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 계정을 탈퇴 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 인증 정보 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "409", description = "회원 탈퇴 불가")
    })
    @DeleteMapping("/me")
    public ResponseEntity<CommonResponse<UserDeleteResponse>> deleteMyAccount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDeleteRequest request
    ) {
        String loginId = userDetails.getUsername();

        UserDeleteResponse response =
                userService.deleteMyAccount(loginId, request);

        return ResponseEntity.ok(
                CommonResponse.success("회원 탈퇴 성공", response)
        );
    }
}
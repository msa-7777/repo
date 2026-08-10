package com.sparta.userservice.presentation.controller;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: 내 정보 조회
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.service.UserService;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.presentation.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "회원 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class UserQueryController {

    private final UserService userService;

    // 1-3 내 정보 조회
    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자의 회원 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserInfoResponse>> getMyInfo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();

        UserInfoResponse response = userService.getMyInfo(loginId);

        return ResponseEntity.ok(
                CommonResponse.success("내 정보 조회 성공", response)
        );
    }
}
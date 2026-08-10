package com.sparta.userservice.presentation.controller;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-10
 * 설명: 회원가입, 로그인
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.service.UserAuthService;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.presentation.dto.request.UserLoginRequest;
import com.sparta.userservice.presentation.dto.request.UserSignupRequest;
import com.sparta.userservice.presentation.dto.response.UserLoginResponse;
import com.sparta.userservice.presentation.dto.response.UserSignupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "User Auth", description = "회원 인증 API")
public class UserAuthController {

    private final UserAuthService userAuthService;

    // 1-1 회원가입 요청
    @Operation(
            summary = "회원가입 요청",
            description = "회원가입을 요청합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 요청 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "중복된 회원 정보")
    })
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<UserSignupResponse>> signup(
            @Valid @RequestBody UserSignupRequest request
    ) {
        UserSignupResponse response = userAuthService.signup(request);

        return ResponseEntity.ok(
                CommonResponse.success("회원가입 요청 성공", response)
        );
    }

    // 1-2 로그인(JWT 발급)
    @Operation(
            summary = "로그인",
            description = "로그인 후 JWT Access Token을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "로그인 정보 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<UserLoginResponse>> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        UserLoginResponse response = userAuthService.login(request);

        return ResponseEntity.ok(
                CommonResponse.success("로그인 성공", response)
        );
    }
}

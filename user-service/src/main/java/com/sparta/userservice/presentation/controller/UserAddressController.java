package com.sparta.userservice.presentation.controller;

import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.application.service.UserAddressService;
import com.sparta.userservice.domain.model.User;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.presentation.dto.request.UserAddressCreateRequest;
import com.sparta.userservice.presentation.dto.response.UserAddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/addresses")
@Tag(name = "User Address", description = "사용자 주소 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final UserFinder userFinder;

    @PostMapping
    @Operation(
            summary = "내 주소 등록",
            description = "로그인한 사용자의 주소를 등록합니다. 첫 번째로 등록되는 주소는 자동으로 기본 주소로 설정됩니다."
    )
    public CommonResponse<UserAddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody UserAddressCreateRequest request
    ) {
        String loginId = authentication.getName();

        User user = userFinder.getUserByLoginId(loginId);

        UUID userId = user.getUserId();

        return CommonResponse.success(
                "주소 등록 성공",
                userAddressService.createAddress(userId, request)
        );
    }
}
package com.sparta.userservice.presentation.controller;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-11
 * 설명: 외부 서비스가 사용자 정보를 사용하기 위한 단건 조회 / 존재 유무
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

import com.sparta.userservice.application.service.InternalUserService;
import com.sparta.userservice.global.response.CommonResponse;
import com.sparta.userservice.presentation.dto.response.InternalUserResponse;
import com.sparta.userservice.presentation.dto.response.UserAddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
@Tag(name = "Internal User", description = "서비스 내부 사용자 조회 API")
public class InternalUserController {

    private final InternalUserService internalUserService;

    // 2-1 사용자 단건 조회
    @GetMapping("/{userId}")
    @Operation(
            summary = "내부 사용자 단건 조회",
            description = "다른 서비스에서 userId를 이용해 사용자 정보를 조회합니다."
    )
    public ResponseEntity<CommonResponse<InternalUserResponse>> getUser(
            @PathVariable UUID userId) {

        InternalUserResponse response = internalUserService.getUser(userId);

        return ResponseEntity.ok(CommonResponse.success("내부 사용자 조회 성공", response));
    }

    // 2-2 해당 허브를 참조하고 있는 User 존재 여부 반환
    @GetMapping("/exists")
    @Operation(
            summary = "허브 소속 사용자 존재 여부 조회",
            description = "다른 서비스에서 hubId를 이용해 해당 허브를 참조하는 사용자가 존재하는지 확인합니다."
    )
    public boolean existsUserByHubId(@RequestParam UUID hubId) {

        return internalUserService.existsUserByHubId(hubId);
    }

    // 2-3 다음 순번의 배송 담당자 조회에 필요한
    // hubId 소속 DELIVERY_AGENT 에 대한 user_id 리스트 반환
    @GetMapping("/delivery-managers")
    @Operation(
            summary = "허브 배송 담당자 ID 목록 조회",
            description = "다른 서비스에서 hubId를 이용해 해당 허브에 소속된 DELIVERY_AGENT의 userId 목록을 조회합니다."
    )
    public List<UUID> getDeliveryManagerIds(@RequestParam UUID hubId) {

        return internalUserService.getDeliveryManagerIds(hubId);
    }

    // 2-4 user_id에 대한 default address 전송
    @GetMapping("/{userId}/default-address")
    @Operation(
            summary = "사용자 기본 주소 내부 조회",
            description = "다른 서비스에서 userId를 이용해 사용자의 기본 주소를 조회합니다."
    )
    public CommonResponse<UserAddressResponse> getDefaultAddress(@PathVariable UUID userId) {

        return CommonResponse.success("사용자 기본 주소 조회 성공", internalUserService.getDefaultAddress(userId));
    }
}
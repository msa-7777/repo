package com.msa7.hub.presentation.api;

import com.msa7.hub.presentation.response.HubRoutePathResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Internal - Hub", description = "서비스 간 내부 호출 전용 API (인증 불필요)")
public interface InternalHubApi {

    @Operation(summary = "허브 존재 여부 확인", description = "다른 서비스가 hubId 유효성을 검증할 때 사용")
    @ApiResponse(responseCode = "200", description = "존재 여부(boolean)")
    @GetMapping("/{hubId}/exists")
    boolean checkHubExists(@Parameter(description = "허브 ID") @PathVariable UUID hubId);

    @Operation(summary = "두 허브 간 최적 경로 조회", description = "출발/도착 허브 간 이동 경로 및 소요시간 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{startHubId}/routes/{endHubId}")
    HubRoutePathResponse getRouteInfo(
            @Parameter(description = "출발 허브 ID") @PathVariable UUID startHubId,
            @Parameter(description = "도착 허브 ID") @PathVariable UUID endHubId
    );
}
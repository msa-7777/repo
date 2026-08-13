package com.msa7.hub.presentation.api;

import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.request.HubRouteSearchRequest;
import com.msa7.hub.presentation.response.HubRouteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "HubRoute", description = "허브 간 이동 경로(라우트) 관리 API")
public interface HubRouteApi {

    @Operation(summary = "허브 라우트 생성", description = "두 허브 간 이동 경로를 생성한다. MASTER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(MASTER 아님)")
    })
    ResponseEntity<RestApiResponse<HubRouteResponse>> createHubRoute(
            @RequestBody @Valid HubRouteRequest request
    );

    @Operation(summary = "허브 라우트 수정", description = "기존 라우트 정보를 수정한다. MASTER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(MASTER 아님)"),
            @ApiResponse(responseCode = "404", description = "라우트를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<HubRouteResponse>> updateHubRoute(
            @Parameter(description = "허브 라우트 ID") UUID hubRouteId,
            @RequestBody @Valid HubRouteRequest request
    );

    @Operation(summary = "허브 라우트 삭제", description = "라우트를 soft delete한다. MASTER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음(MASTER 아님)"),
            @ApiResponse(responseCode = "404", description = "라우트를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<Void>> deleteHubRoute(
            @Parameter(description = "허브 라우트 ID") UUID hubRouteId,
            @AuthenticationPrincipal UUID userId
    );

    @Operation(summary = "허브 라우트 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "라우트를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<HubRouteResponse>> getHubRoute(
            @Parameter(description = "허브 라우트 ID") UUID hubRouteId
    );

    @Operation(summary = "허브 라우트 목록 조회", description = "출발/도착 허브 ID로 검색 가능")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<RestApiResponse<Page<HubRouteResponse>>> getHubRouteList(
            @ModelAttribute HubRouteSearchRequest request,
            Pageable pageable
    );
}
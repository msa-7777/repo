package com.msa7.hub.presentation.api;

import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.request.HubSearchRequest;
import com.msa7.hub.presentation.response.HubResponse;
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

@Tag(name = "Hub", description = "허브 관리 API")
public interface HubApi {

    @Operation(summary = "허브 생성", description = "새로운 허브를 생성한다. MASTER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(MASTER 아님)")
    })
    ResponseEntity<RestApiResponse<HubResponse>> createHub(
            @RequestBody @Valid HubRequest request
    );

    @Operation(summary = "허브 수정", description = "기존 허브 정보를 수정한다. MASTER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(MASTER 아님)"),
            @ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<HubResponse>> updateHub(
            @RequestBody @Valid HubRequest request,
            @Parameter(description = "허브 ID") UUID hubId
    );

    @Operation(summary = "허브 삭제", description = "허브를 soft delete한다. 참조 중인 업체/재고/사용자/배송이 있으면 차단된다. MASTER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음(MASTER 아님)"),
            @ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "다른 리소스가 참조 중이라 삭제 불가")
    })
    ResponseEntity<RestApiResponse<Void>> deleteHub(
            @Parameter(description = "허브 ID") UUID hubId,
            @AuthenticationPrincipal UUID userId
    );

    @Operation(summary = "허브 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음")
    })
    ResponseEntity<RestApiResponse<HubResponse>> getHub(
            @Parameter(description = "허브 ID") UUID hubId
    );

    @Operation(summary = "허브 목록 조회", description = "이름/주소/중앙허브 여부로 검색 가능")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<RestApiResponse<Page<HubResponse>>> getHubList(
            @ModelAttribute HubSearchRequest request,
            Pageable pageable
    );
}
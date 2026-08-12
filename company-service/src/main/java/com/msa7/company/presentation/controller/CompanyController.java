package com.msa7.company.presentation.controller;

import com.msa7.company.application.CompanyApplicationService;
import com.msa7.company.global.response.RestApiResponse;
import com.msa7.company.presentation.dto.request.CompanySearchCondition;
import com.msa7.company.presentation.dto.request.CreateCompanyRequest;
import com.msa7.company.presentation.dto.request.UpdateCompanyRequest;
import com.msa7.company.presentation.dto.response.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Company API", description = "업체 관리 API")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private static final String ROLE_PREFIX = "ROLE_";
    private final CompanyApplicationService companyApplicationService;

    // 1. 업체 등록 (마스터 관리자, 허브 관리자)
    @Operation(summary = "업체 생성", description = "새로운 업체 정보를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업체 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        CompanyResponse response = companyApplicationService.createCompany(request, userId, role);
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP Status 201 명시
                .body(RestApiResponse.ok(HttpStatus.CREATED, "업체가 성공적으로 등록되었습니다.", response));
    }

    // 2. 업체 단건 조회
    @Operation(summary = "업체 단건 조회", description = "특정 업체의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "업체를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole()")
    @GetMapping("/{companyId}")
    public ResponseEntity<RestApiResponse<CompanyResponse>> getCompany(
            @PathVariable UUID companyId
    ) {
        CompanyResponse response = companyApplicationService.getCompany(companyId);
        return ResponseEntity.ok(
                RestApiResponse.ok(HttpStatus.OK, "업체 정보 조회가 완료되었습니다.", response)
        );
    }

    // 3. 업체 목록 동적 검색 및 페이징
    @Operation(summary = "업체 목록 조회 및 검색", description = "업체 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    @PreAuthorize("hasAnyRole()")
    @GetMapping
    public ResponseEntity<RestApiResponse<Page<CompanyResponse>>> getCompanies(
            @ModelAttribute CompanySearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CompanyResponse> response = companyApplicationService.searchCompanies(condition, pageable);
        return ResponseEntity.ok(
                RestApiResponse.ok(HttpStatus.OK, "업체 목록 조회가 완료되었습니다.", response)
        );
    }

    // 4. 업체 정보 수정
    @Operation(summary = "업체 정보 수정", description = "업체 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "업체를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'SUPPLIER_AGENT')")
    @PatchMapping("/{companyId}")
    public ResponseEntity<RestApiResponse<CompanyResponse>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        CompanyResponse response = companyApplicationService.updateCompany(companyId, request, userId, role);
        return ResponseEntity.ok(
                RestApiResponse.ok(HttpStatus.OK, "업체 정보가 정상적으로 수정되었습니다.", response)
        );
    }

    // 5. 업체 삭제 (200 OK - Soft Delete)
    @Operation(summary = "업체 삭제", description = "업체를 삭제(논리 삭제)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "업체를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'SUPPLIER_AGENT')")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<RestApiResponse<Void>> deleteCompany(
            @PathVariable UUID companyId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        companyApplicationService.deleteCompany(companyId, userId, role);
        return ResponseEntity.ok(
                RestApiResponse.ok(HttpStatus.OK, "업체가 성공적으로 삭제되었습니다.", null)
        );
    }


    // --- Helper Methods ---
    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .findFirst()
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .orElseThrow(() -> new IllegalArgumentException("권한 정보를 찾을 수 없습니다."));
    }
}
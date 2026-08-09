package com.sparta.productservice.presentation.product;

import com.sparta.productservice.application.product.ProductService;
import com.sparta.productservice.domain.product.ProductSearchCondition;
import com.sparta.productservice.global.response.RestApiResponse;
import com.sparta.productservice.global.security.HeaderAuthenticationFilter;
import com.sparta.productservice.presentation.product.request.ProductCreateRequest;
import com.sparta.productservice.presentation.product.request.ProductUpdateRequest;
import com.sparta.productservice.presentation.product.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.swing.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final String ROLE_PREFIX = "ROLE_";

    private final ProductService productService;

    // 상품 생성
    // 담당 허브 또는 본인 업체인지에 대한 세부 검증은 ProductService에서 userId와 role을 기준으로 수행한다.
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @PostMapping
    public ResponseEntity<RestApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        // 상품 생성 시 company-service에서 업체 정보를 조회하고,업체 소속 hubId를 사용해 초기 재고를 함께 생성한다.

        /*
         * Service에서 다음 작업을 수행한다.
         *
         * 1. 요청자의 상품 생성 범위 검증
         *    - MASTER: 전체 허용
         *    - HUB_MANAGER: 담당 허브 업체만 허용
         *    - COMPANY_MANAGER: 본인 업체만 허용
         * 2. 업체 존재 여부 및 PRODUCER 타입 확인
         * 3. 업체의 hubId 확인
         * 4. 상품 생성
         * 5. 초기 재고 0 생성
         */

        ProductResponse response =
                productService.createProduct(request, userId, role);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED,
                                "상품이 생성되었습니다.",
                                response
                        )
                );
    }

    // 상품 단건 조회
    /* 상품 조회 자체는 모든 로그인 사용자가 가능하다.
    * HUB_MANAGER의 경우 담당 허브 상품인지 여부는 Service에서 검증한다.
    */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{productId}")
    public ResponseEntity<RestApiResponse<ProductResponse>> getProduct(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        ProductResponse response =
                productService.getProduct(productId, userId, role);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품이 조회되었습니다.",
                        response
                )
        );
    }

    // 상품 목록 조회
    /* 모든 로그인 사용자가 호출할 수 있다.
    * HUB_MANAGER인 경우 담당 허브 상품만 조회되도록 Service/Querydsl에서 검색 조건을 추가한다.
    */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<RestApiResponse<Page<ProductResponse>>> getProducts(
            @ModelAttribute ProductSearchCondition condition,
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        Page<ProductResponse> response =
                productService.getProducts(
                        condition,
                        pageable,
                        userId,
                        role
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품 목록이 조회되었습니다.",
                        response
                )
        );
    }

    // 상품명 수정
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @PatchMapping("/{productId}")
    public ResponseEntity<RestApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        ProductResponse response =
                productService.updateProduct(
                        productId,
                        request,
                        userId,
                        role
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품이 수정되었습니다.",
                        response
                )
        );
    }

    // 상품 논리 삭제
    // HUB_MANAGER가 실제 담당 허브의 상품을 삭제하는지는 ProductService에서 추가 검증
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<RestApiResponse<Void>> deleteProduct(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        String role = getRole(authentication);

        productService.deleteProduct(
                productId,
                userId,
                role
        );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품이 삭제되었습니다.",
                        null
                )
        );
    }


    // HeaderAuthenticationFilter가 principal에 저장한 userId를 가져온다.
    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private String getRole(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .findFirst()
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .orElseThrow(() ->
                        new IllegalStateException("인증된 사용자의 권한 정보를 찾을 수 없습니다.")
                );
    }
}
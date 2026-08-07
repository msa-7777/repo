package com.sparta.productservice.presentation.product;

import com.sparta.productservice.application.product.ProductService;
import com.sparta.productservice.domain.product.ProductSearchCondition;
import com.sparta.productservice.global.response.RestApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    /*
     * TODO: 인증 기능 구현 후 삭제 요청자의 실제 사용자 ID를 사용한다.
     *
     * 현재는 로컬 CRUD 테스트를 위한 임시 사용자 ID이며,
     * 추후 Gateway 또는 JWT에서 전달받은 사용자 ID로 교체해야 한다.
     */
    private static final UUID TEMPORARY_USER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private final ProductService productService;

    // 상품 생성
    // TODO: 인증·인가 적용 후 MASTER, 담당 HUB_MANAGER, 본인 업체의 COMPANY_MANAGER만 접근하도록 제한한다.
    @PostMapping
    public ResponseEntity<RestApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        /*
         * 상품 생성 시 company-service에서 업체 정보를 조회하고,
         * 업체 소속 hubId를 사용해 초기 재고를 함께 생성한다.
         *
         * TODO: 인증·인가 적용 후 MASTER, 담당 HUB_MANAGER,
         *       본인 업체 COMPANY_MANAGER만 생성할 수 있도록 검증한다.
         */

        /*
         * Service 내부에서 다음 작업을 수행한다.
         *
         * 1. 업체 단건 조회
         * 2. PRODUCER 및 hubId 검증
         * 3. 상품 생성
         * 4. 초기 재고 0 생성
         */

        ProductResponse response =
                productService.createProduct(request);

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
    // TODO: 인증 적용 후 역할별 상품 조회 범위를 Service에서 검증한다.
    @GetMapping("/{productId}")
    public ResponseEntity<RestApiResponse<ProductResponse>> getProduct(
            @PathVariable UUID productId
    ) {
        ProductResponse response =
                productService.getProduct(productId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품이 조회되었습니다.",
                        response
                )
        );
    }

    // 상품 목록 조회
    // TODO: 허용할 page size와 정렬 필드를 제한한다.
    // TODO: 로그인 사용자의 역할과 소속에 따라 조회 가능한 상품 범위를 Querydsl 조건에 추가한다.
    @GetMapping
    public ResponseEntity<RestApiResponse<Page<ProductResponse>>> getProducts(
            @ModelAttribute ProductSearchCondition condition,
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<ProductResponse> response =
                productService.getProducts(condition, pageable);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품 목록이 조회되었습니다.",
                        response
                )
        );
    }

    // 상품명 수정
    // TODO: 인증·인가 적용 후 상품 수정 권한을 검증한다.
    // TODO: 상품 수정 범위는 상품명만 유지한다.
    @PatchMapping("/{productId}")
    public ResponseEntity<RestApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response =
                productService.updateProduct(
                        productId,
                        request
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
    // TODO: 임시 사용자 ID를 제거하고 인증된 사용자 ID를 Service에 전달한다.
    // TODO: MASTER 또는 담당 허브 관리자만 삭제 가능하도록 권한을 적용한다.
    @DeleteMapping("/{productId}")
    public ResponseEntity<RestApiResponse<Void>> deleteProduct(
            @PathVariable UUID productId
    ) {
        productService.deleteProduct(
                productId,
                TEMPORARY_USER_ID
        );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "상품이 삭제되었습니다.",
                        null
                )
        );
    }
}
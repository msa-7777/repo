package com.sparta.productservice.application.product;

import com.sparta.productservice.application.inventory.InventoryService;
import com.sparta.productservice.domain.product.Product;
import com.sparta.productservice.domain.product.ProductRepository;
import com.sparta.productservice.domain.product.ProductSearchCondition;
import com.sparta.productservice.global.exception.ApiException;
import com.sparta.productservice.global.exception.product.ProductErrorCode;
import com.sparta.productservice.infrastructure.client.company.CompanyClient;
import com.sparta.productservice.infrastructure.client.company.CompanyResponse;
import com.sparta.productservice.presentation.product.request.ProductCreateRequest;
import com.sparta.productservice.presentation.product.request.ProductUpdateRequest;
import com.sparta.productservice.presentation.product.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    // private final CompanyClient companyClient;

    /*
     * TODO: company-service 연동 완료 후 제거한다.
     * 로컬 테스트에서만 사용하는 임시 허브 ID다.
     */
    private static final UUID TEMPORARY_HUB_ID =
            UUID.fromString("f6a7b8c9-0000-0000-0000-000000000001");

    // 상품 생성 - 상품과 초기 재고를 함께 생성한다.
    // TODO: company-service 연동 후 상품 생성 전에 companyId가 실제 존재하는 업체인지 확인한다.
    // TODO: 업체 담당자는 자신이 소속된 업체의 상품만 생성할 수 있도록 권한과 소유권을 검증한다.
    // TODO: 허브 관리자는 담당 허브 소속 업체의 상품만 생성할 수 있도록 업체·허브 관계를 검증한다.
    // TODO: 다른 서비스 호출 실패 시 Feign 예외 처리 및 공통 에러 응답 정책을 적용한다.

    /* 현재 로컬 테스트 단계:
     * - 요청으로 받은 companyId를 그대로 사용한다.
     * - 임시 hubId로 초기 수량 0의 재고를 생성한다.
     *
     * 최종 연동 단계:
     * - company-service의 업체 단건 조회 API를 호출한다.
     * - PRODUCER 업체인지 검증한다.
     * - 업체 응답의 hubId로 재고를 생성한다.

    /* 처리 순서:
     * 1. company-service 업체 단건 조회
     * 2. 생산업체 여부 및 hubId 검증
     * 3. 상품명 중복 검사
     * 4. Product 저장
     * 5. 초기 수량 0의 Inventory 저장
     *
     * Product와 Inventory는 같은 product-service DB를 사용하므로
     * 재고 생성에 실패하면 상품 저장도 함께 롤백된다.
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {

        /*
         * TODO: company-service 연동 후 기존 업체 단건 조회 API를 호출한다.
         * TODO: PRODUCER 업체인지 검증하고 응답의 hubId로 초기 재고를 생성한다.
         */

        /*
         * TODO [최종 MSA 연동]:
         * 1. Eureka를 통해 company-service를 조회한다.
         * 2. 기존 업체 단건 조회 API를 FeignClient로 호출한다.
         * 3. 요청 companyId와 응답 companyId가 일치하는지 확인한다.
         * 4. companyType이 PRODUCER인지 검증한다.
         * 5. 응답의 hubId가 존재하는지 확인한다.
         * 6. 아래 TEMPORARY_HUB_ID를 제거하고 company.hubId()를 사용한다.
         * 7. 업체 미존재, 잘못된 업체 유형, 서비스 통신 실패를
         *    각각 공통 예외 응답으로 구분한다.
         */

        validateDuplicateProductName(request.companyId(), request.name());

        Product product = Product.create(request.companyId(), request.name());

        Product savedProduct = productRepository.save(product);

        //  company-service 연동 전까지 임시 hubId를 사용해 상품과 초기 재고 생성 흐름만 로컬에서 검증한다.
         /*
         * TODO: 업체 단건 조회 API 연결 후 TEMPORARY_HUB_ID를 제거하고,
         *       company.hubId()를 사용한다.
         */
        inventoryService.createInventory(
                savedProduct.getId(),
                TEMPORARY_HUB_ID
        );

        /*
         * TODO: 상품 저장과 재고 저장은 같은 트랜잭션에서 처리하여
         *       재고 생성 실패 시 상품 생성도 롤백되도록 한다.
         */
        // TODO: 인증 적용 후 MASTER, 담당 HUB_MANAGER,
        //       본인 업체 COMPANY_MANAGER만 상품을 생성하도록 검증한다.
        // TODO: Feign 호출 타임아웃 정책을 적용한다.

        return ProductResponse.from(savedProduct);
    }

    // 업체 단건 조회 API를 호출하고 상품 생성에 필요한 업체 정보를 검증한다.

    // 상품 단건 조회
    // TODO: 인증·인가 적용 후 사용자 역할에 따른 상품 조회 범위를 검증한다.
    // TODO: 허브 관리자는 담당 허브 상품만 조회하도록 company-service 또는 관련 서비스와 연동한다.
    public ProductResponse getProduct(UUID productId) {

        Product product = findActiveProduct(productId);

        return ProductResponse.from(product);
    }

    // 상품 목록 조회 - 삭제되지 않은 상품만 조회
    // TODO: 허용할 page size와 정렬 필드를 제한한다.
    // TODO: 로그인 사용자의 역할과 소속에 따라 조회 가능한 상품 범위를 Querydsl 조건에 추가한다.
    public Page<ProductResponse> getProducts(
            ProductSearchCondition condition,
            Pageable pageable
    ) {
        return productRepository
                .searchProducts(condition, pageable)
                .map(ProductResponse::from);
    }

    //상품 수정 - 현재 수정 가능한 필드는 상품명뿐
    @Transactional
    public ProductResponse updateProduct(
            UUID productId,
            ProductUpdateRequest request
    ) {
        Product product = findActiveProduct(productId);

        validateDuplicateProductNameForUpdate(
                product.getCompanyId(),
                request.name(),
                productId
        );

        product.updateName(request.name());

        /* product는 영속 상태이므로 별도의 save()를 호출하지 않아도
        * 트랜잭션이 종료될 때 변경 감지로 UPDATE 쿼리가 실행
        */
        return ProductResponse.from(product);
    }

    // 상품 논리 삭제 시, 연결된 재고를 함께 논리 삭제한다
    // 연결된 재고가 없더라도 상품을 삭제한다
    @Transactional
    public void deleteProduct(
            UUID productId,
            UUID deletedBy
    ) {
        Product product = findActiveProduct(productId);

        /*
         * 상품과 재고는 같은 product-service에서 관리하므로
         * 하나의 로컬 트랜잭션 안에서 함께 논리 삭제한다.
         */
        product.delete(deletedBy);
        inventoryService.deleteInventoryIfExists(productId, deletedBy);

        // TODO: 주문 등 다른 서비스의 연관 데이터 비활성화 정책을 확정한다.
        // TODO: MSA 연동 시 다른 서비스의 삭제 처리는 이벤트 또는 API 호출로 전달한다.
    }

    // 삭제되지 않은 상품 조회 - 상품이 없거나 이미 삭제된 경우 모두 PRODUCT_NOT_FOUND로 처리
    private Product findActiveProduct(UUID productId) {

        return productRepository
                .findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() ->
                        new ApiException(
                                ProductErrorCode.PRODUCT_NOT_FOUND
                        )
                );
    }

    // 상품 생성 시 동일 업체 내 상품명 중복 검사
    private void validateDuplicateProductName(
            UUID companyId,
            String name
    ) {
        boolean duplicated = productRepository
                .existsByCompanyIdAndNameAndDeletedAtIsNull(
                                companyId,
                                name
                        );

        if (duplicated) {
            throw new ApiException(ProductErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    // 상품 수정 시 자기 자신을 제외한 상품명 중복 검사
    private void validateDuplicateProductNameForUpdate(
            UUID companyId,
            String name,
            UUID productId
    ) {
        boolean duplicated = productRepository
                        .existsByCompanyIdAndNameAndIdNotAndDeletedAtIsNull(
                                companyId,
                                name,
                                productId
                        );

        if (duplicated) {
            throw new ApiException(ProductErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }
}
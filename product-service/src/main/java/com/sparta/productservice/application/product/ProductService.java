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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    //Search 공통 요구사항
    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_PAGE_SIZE = 10;


    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CompanyClient companyClient;

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
     */

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
    public ProductResponse createProduct(ProductCreateRequest request,
                                         UUID userId,
                                         String role) {

        validateCreateAccess(request.companyId(), userId, role);
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

        // TODO: Feign 호출 타임아웃 정책을 적용한다.

        return ProductResponse.from(savedProduct);
    }

    // 업체 단건 조회 API를 호출하고 상품 생성에 필요한 업체 정보를 검증한다.

    // 상품 단건 조회
    public ProductResponse getProduct(UUID productId,
                                      UUID userId,
                                      String role) {

        Product product = findActiveProduct(productId);

        // 인증·인가 적용 후 사용자 역할에 따른 상품 조회 범위를 검증
        validateReadAccess(product, userId, role);

        return ProductResponse.from(product);
    }

    // 상품 목록 조회 - 삭제되지 않은 상품만 조회
    // TODO: 로그인 사용자의 역할과 소속에 따라 조회 가능한 상품 범위를 Querydsl 조건에 추가한다.
    public Page<ProductResponse> getProducts(
            ProductSearchCondition condition,
            Pageable pageable,
            UUID userId,
            String role
    ) {
        Pageable normalizedPageable = normalizePageable(pageable);

        /*
         * TODO [역할별 조회 범위]:
         *
         * HUB_MANAGER
         * - userId의 담당 hubId를 조회
         * - Inventory.hubId와 연결해서 해당 허브 상품만 조회
         *
         * MASTER / DELIVERY_MANAGER / COMPANY_MANAGER
         * - 전체 조회 허용
         */

        return productRepository
                .searchProducts(condition, normalizedPageable)
                .map(ProductResponse::from);
    }

    //상품 수정 - 현재 수정 가능한 필드는 상품명뿐
    @Transactional
    public ProductResponse updateProduct(
            UUID productId,
            ProductUpdateRequest request,
            UUID userId,
            String role
    ) {
        Product product = findActiveProduct(productId);

        /*
         * TODO [리소스 권한 검증]:
         *
         * MASTER
         * - 모든 상품 수정 가능
         *
         * HUB_MANAGER
         * - 해당 상품의 Inventory hubId가 본인의 담당 hubId인지 확인
         *
         * COMPANY_MANAGER
         * - product.companyId와 본인 companyId가 동일한지 확인
         */

        validateUpdateAccess(product, userId, role);

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
            UUID userId,
            String role
    ) {
        Product product = findActiveProduct(productId);

        /*
         * TODO [리소스 권한 검증]:
         *
         * MASTER
         * - 모든 상품 삭제 가능
         *
         * HUB_MANAGER
         * - 해당 상품의 Inventory hubId와
         *   본인의 담당 hubId가 동일한지 확인
         */
        validateDeleteAccess(product, userId, role);

        /*
         * 상품과 재고는 같은 product-service에서 관리하므로
         * 하나의 로컬 트랜잭션 안에서 함께 논리 삭제한다.
         */
        product.delete(userId);
        inventoryService.deleteInventoryIfExists(productId, userId);

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


    // 상품 목록 조회에 사용할 Pageable을 공통 요구사항에 맞게 보정한다.
    private Pageable normalizePageable(Pageable pageable) {

        // 10, 30, 50이 아닌 size 요청은 기본 10건으로 변경한다.
        int pageSize = ALLOWED_PAGE_SIZES.contains(pageable.getPageSize())
                ? pageable.getPageSize()
                : DEFAULT_PAGE_SIZE;


        // 요청한 page 번호는 유지하고 검증된 size와 sort로 새로운 Pageable을 생성한다.
        return PageRequest.of(
                pageable.getPageNumber(),
                pageSize
        );
    }

    // 상품 CRUD+Search 리소스 검증

    // 상품 생성 리소스 접근 검증
    // "해당 사용자가 요청한 업체/허브 범위까지 접근 가능한가"를 검증한다.
    private void validateCreateAccess(
            UUID companyId,
            UUID userId,
            String role
    ) {
        if ("MASTER".equals(role)) {
            return;
        }

        if ("HUB_MANAGER".equals(role)) {
            /*
             * TODO [user-service 연동]:
             * userId로 HUB_MANAGER의 담당 hubId를 조회한다.
             *
             * TODO [company-service 연동]:
             * companyId로 업체를 조회하고 해당 업체의 hubId를 확인한다.
             * 담당 hubId와 업체 hubId가 다르면 접근 거부한다.
             */
            validateHubManagerCompanyAccess(companyId, userId);
            return;
        }

        if ("COMPANY_MANAGER".equals(role)) {
            /*
             * TODO [user-service 연동]:
             * userId로 COMPANY_MANAGER의 소속 companyId를 조회한다.
             * 요청 companyId와 본인 companyId가 다르면 접근 거부한다.
             */
            validateCompanyManagerCompanyAccess(companyId, userId);
            return;
        }

        throw new ApiException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
    }


    // 상품 단건 조회 리소스 접근 검증
    private void validateReadAccess(
            Product product,
            UUID userId,
            String role
    ) {
        if ("HUB_MANAGER".equals(role)) {
            /*
             * TODO [user-service 연동]:
             * userId로 담당 hubId 조회
             *
             * TODO [Inventory 조회]:
             * product.getId()로 Inventory 조회
             * 두 hubId가 다르면 접근 거부
             */
            validateHubManagerProductAccess(product.getId(), userId);
        }
    }


    // 상품 수정 리소스 접근 검증
    private void validateUpdateAccess(
            Product product,
            UUID userId,
            String role
    ) {
        if ("MASTER".equals(role)) {
            return;
        }

        if ("HUB_MANAGER".equals(role)) {
            validateHubManagerProductAccess(product.getId(), userId);
            return;
        }

        if ("COMPANY_MANAGER".equals(role)) {
            validateCompanyManagerCompanyAccess(
                    product.getCompanyId(),
                    userId
            );
            return;
        }

        throw new ApiException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
    }


    // 상품 삭제 리소스 접근 검증
    private void validateDeleteAccess(
            Product product,
            UUID userId,
            String role
    ) {
        if ("MASTER".equals(role)) {
            return;
        }

        if ("HUB_MANAGER".equals(role)) {
            validateHubManagerProductAccess(product.getId(), userId);
            return;
        }

        throw new ApiException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
    }


    // 공통 세부 검증
    // COMPANY_MANAGER가 실제 본인 업체에 접근하는지 검증한다.
    private void validateCompanyManagerCompanyAccess(
            UUID targetCompanyId,
            UUID userId
    ) {
        /*
         * TODO [user-service 연동]:
         *
         * UUID managerCompanyId =
         *         userClient.getUser(userId).companyId();
         *
         * if (!targetCompanyId.equals(managerCompanyId)) {
         *     throw new ApiException(
         *             ProductErrorCode.PRODUCT_ACCESS_DENIED
         *     );
         * }
         */
    }

    // HUB_MANAGER가 해당 상품이 속한 허브를 담당하는지 검증한다.
    private void validateHubManagerProductAccess(
            UUID productId,
            UUID userId
    ) {
        /*
         * TODO [user-service 연동]:
         * UUID managerHubId =
         *         userClient.getUser(userId).hubId();
         *
         * TODO [Inventory 조회]:
         * Inventory inventory =
         *         inventoryService.getActiveInventory(productId);
         *
         * if (!inventory.getHubId().equals(managerHubId)) {
         *     throw new ApiException(
         *             ProductErrorCode.PRODUCT_ACCESS_DENIED
         *     );
         * }
         */
    }

    // HUB_MANAGER가 해당 업체가 속한 허브를 담당하는지 검증한다.
    // 상품 생성 전에는 아직 Inventory가 없으므로company-service의 hubId를 기준으로 검증한다.
    private void validateHubManagerCompanyAccess(
            UUID companyId,
            UUID userId
    ) {
        /*
         * TODO [user-service 연동]:
         * UUID managerHubId =
         *         userClient.getUser(userId).hubId();
         *
         * TODO [company-service 연동]:
         * CompanyResponse company =
         *         companyClient.getCompany(companyId).getData();
         *
         * if (!company.hubId().equals(managerHubId)) {
         *     throw new ApiException(
         *             ProductErrorCode.PRODUCT_ACCESS_DENIED
         *     );
         * }
         */
    }

}
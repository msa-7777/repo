package com.sparta.productservice.application.product;

import com.sparta.productservice.application.inventory.InventoryService;
import com.sparta.productservice.domain.product.Product;
import com.sparta.productservice.domain.product.ProductRepository;
import com.sparta.productservice.domain.product.ProductSearchCondition;
import com.sparta.productservice.global.exception.ApiException;
import com.sparta.productservice.global.exception.product.ProductErrorCode;
import com.sparta.productservice.infrastructure.client.company.CompanyClient;
import com.sparta.productservice.infrastructure.client.company.CompanyResponse;
import com.sparta.productservice.infrastructure.client.company.CompanyType;
import com.sparta.productservice.infrastructure.client.user.UserClient;
import com.sparta.productservice.infrastructure.client.user.UserResponse;
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
    private final UserClient userClient;

    // 상품 생성 - 상품과 초기 재고를 함께 생성한다.
    // TODO: 다른 서비스 호출 실패 시 Feign 예외 처리 및 공통 에러 응답 정책을 적용한다.

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

        // 상품을 등록할 업체 정보를 company-service에서 조회한다.
        CompanyResponse company = companyClient
                                    .getCompany(request.companyId())
                                    .data();

        // 요청한 업체와 조회된 업체가 동일한지 검증
        validateCompanyId(request.companyId(), company);

        // 생산업체(PRODUCER)인지 검증
        validateProducerCompany(company);

        // 업체에 소속된 hubId가 존재하는지 검증
        validateCompanyHub(company);

        // 요청 사용자가 해당 업체의 상품을 생성할 권한이 있는지 확인
        validateCreateAccess(company, userId, role);

        // 동일 업체 내 상품명 중복 검증
        validateDuplicateProductName(request.companyId(), request.name());

        // 상품 생성 및 저장
        Product product = Product.create(request.companyId(), request.name());
        Product savedProduct = productRepository.save(product);

        // company-service에서 조회한 hubId로 초기 재고 생성
        // 업체의 관리 허브를 기준으로 초기 수량 0의 재고를 함께 생성
        inventoryService.createInventory(
                savedProduct.getId(),
                company.hubId()
        );

        // TODO: Feign 호출 타임아웃 정책을 적용한다.

        return ProductResponse.from(savedProduct);
    }

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
    public Page<ProductResponse> getProducts(
            ProductSearchCondition condition,
            Pageable pageable,
            UUID userId,
            String role
    ) {
        Pageable normalizedPageable = normalizePageable(pageable);

        UUID accessibleHubId = resolveAccessibleHubId(userId, role);

        return productRepository
                .searchProducts(condition, normalizedPageable, accessibleHubId)
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
        // 삭제되지 않은 상품을 조회한다.
        Product product = findActiveProduct(productId);

        // 역할과 소속을 기준으로 해당 상품을 수정할 수 있는지 확인한다.
        validateUpdateAccess(product, userId, role);

        // 동일 업체 내 다른 상품과 이름이 중복되지 않는지 확인한다.
        validateDuplicateProductNameForUpdate(
                product.getCompanyId(),
                request.name(),
                productId
        );

        // 현재 정책상 수정 가능한 필드는 상품명뿐이다.
        product.updateName(request.name());

        /*
         * Product는 영속 상태이므로 별도의 save()를 호출하지 않아도
         * 트랜잭션 종료 시 변경 감지로 UPDATE 쿼리가 실행된다.
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
        // 삭제되지 않은 상품을 조회한다.
        Product product = findActiveProduct(productId);

        // 해당 상품을 삭제할 수 있는 사용자인지 확인한다.
        validateDeleteAccess(product, userId, role);

        // 상품과 연결된 재고를 함께 논리 삭제한다.
        /*
         * Product와 Inventory는 같은 product-service에서 관리하므로
         * 하나의 로컬 트랜잭션 안에서 처리한다.
         */
        product.delete(userId);
        inventoryService.deleteInventoryIfExists(
                productId,
                userId
        );
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


        // 요청한 page 번호는 유지하고 검증된 size로 새로운 Pageable을 생성한다.
        return PageRequest.of(
                pageable.getPageNumber(),
                pageSize
        );
    }

    // 상품 CRUD+Search 리소스 검증

    // 상품 생성 리소스 접근 검증
    // "해당 사용자가 요청한 업체/허브 범위까지 접근 가능한가"를 검증한다.
    private void validateCreateAccess(
            CompanyResponse company,
            UUID userId,
            String role
    ) {
        // MASTER는 모든 업체의 상품을 생성할 수 있다.
        if ("MASTER".equals(role)) {
            return;
        }

        // HUB_MANAGER는 자신이 담당하는 허브에 소속된 업체의 상품만 생성할 수 있다.
        if ("HUB_MANAGER".equals(role)) {
            validateHubManagerCompanyAccess(
                    company.hubId(),
                    userId
            );
            return;
        }

        // SUPPLIER_AGENT는 자신이 소속된 생산 업체의 상품만 생성할 수 있다.
        if ("SUPPLIER_AGENT".equals(role)) {
            validateSupplierAgentCompanyAccess(
                    company.companyId(),
                    userId
            );
            return;
        }

        // DELIVERY_AGENT 등 상품 생성 권한이 없는 역할은 차단한다.
        throw new ApiException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
    }


    // 상품 단건 조회 리소스 접근 검증
    private void validateReadAccess(
            Product product,
            UUID userId,
            String role
    ) {
        // MASTER, SUPPLIER_AGENT, DELIVERY_AGENT는 모든 활성 상품을 조회할 수 있다.

        // HUB_MANAGER만 담당 허브의 상품인지 추가로 확인한다.
        if ("HUB_MANAGER".equals(role)) {
            validateHubManagerProductAccess(
                    product.getId(),
                    userId
            );
        }

        if ("MASTER".equals(role)
                || "SUPPLIER_AGENT".equals(role)
                || "DELIVERY_AGENT".equals(role)) {
            return;
        }

        throw new ApiException(
                ProductErrorCode.PRODUCT_ACCESS_DENIED
        );
    }


    // 상품 목록 조회 리소스 접근 검증
    private UUID resolveAccessibleHubId(
            UUID userId,
            String role
    ) {
        /*
         * HUB_MANAGER만 담당 허브 범위를 제한한다.
         *
         * MASTER, SUPPLIER_AGENT, DELIVERY_AGENT는
         * 전체 활성 상품을 조회할 수 있으므로 null을 전달한다.
         */

        if ("HUB_MANAGER".equals(role)) {
            UserResponse user = getUser(userId);

            if (user.hubId() == null) {
                throw new ApiException(
                        ProductErrorCode.PRODUCT_ACCESS_DENIED
                );
            }

            return user.hubId();
        }

        if ("MASTER".equals(role)
                || "SUPPLIER_AGENT".equals(role)
                || "DELIVERY_AGENT".equals(role)) {
            return null;
        }

        throw new ApiException(
                ProductErrorCode.PRODUCT_ACCESS_DENIED
        );
    }


    // 상품 수정 리소스 접근 검증
    private void validateUpdateAccess(
            Product product,
            UUID userId,
            String role
    ) {
        // MASTER는 모든 상품을 수정할 수 있다.
        if ("MASTER".equals(role)) {
            return;
        }

        // HUB_MANAGER는 담당 허브의 상품만 수정할 수 있다.
        if ("HUB_MANAGER".equals(role)) {
            validateHubManagerProductAccess(
                    product.getId(),
                    userId
            );
            return;
        }

        // SUPPLIER_AGENT는 본인 업체의 상품만 수정할 수 있다.
        if ("SUPPLIER_AGENT".equals(role)) {
            validateSupplierAgentCompanyAccess(
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
    // SUPPLIER_AGENT가 실제 본인 업체에 접근하는지 검증한다.
    private void validateSupplierAgentCompanyAccess(
            UUID targetCompanyId,
            UUID userId
    ) {
        // user-service에서 사용자의 소속 정보를 조회한다.
        UserResponse user = getUser(userId);

         // supplierId가 없거나 요청 대상 companyId와 다르면 다른 업체의 상품에 접근하려는 요청이므로 거부한다.
        if (user.supplierId() == null
                || !targetCompanyId.equals(user.supplierId())) {

            throw new ApiException(
                    ProductErrorCode.PRODUCT_ACCESS_DENIED
            );
        }
    }

    // HUB_MANAGER가 해당 상품이 속한 허브를 담당하는지 검증한다.
    private void validateHubManagerProductAccess(
            UUID productId,
            UUID userId
    ) {
        // user-service에서 허브 관리자의 담당 허브를 조회한다.
        UserResponse user = getUser(userId);

        /*
         * HUB_MANAGER라면 정상적으로 담당 hubId가 존재해야 한다.
         * hubId가 없다면 상품 접근 권한을 판단할 수 없으므로 거부한다.
         */
        if (user.hubId() == null) {
            throw new ApiException(
                    ProductErrorCode.PRODUCT_ACCESS_DENIED
            );
        }

        // 상품에 연결된 활성 재고의 관리 허브를 조회한다.
        /*
         * 상품의 hubId는 Product가 아니라 Inventory에서 관리하므로
         * productId를 기준으로 Inventory의 hubId를 가져온다.
         */
        UUID productHubId =
                inventoryService.getHubIdByProductId(productId);

        // 담당 허브와 상품 관리 허브가 다르면 접근을 거부한다.
        if (!user.hubId().equals(productHubId)) {
            throw new ApiException(
                    ProductErrorCode.PRODUCT_ACCESS_DENIED
            );
        }
    }

    // HUB_MANAGER가 해당 업체가 속한 허브를 담당하는지 검증한다.
    // 상품 생성 전에는 아직 Inventory가 없으므로 company-service의 hubId를 기준으로 검증한다.
    private void validateHubManagerCompanyAccess(
            UUID companyHubId,
            UUID userId
    ) {
        // user-service에서 HUB_MANAGER의 담당 허브 정보를 조회한다.
        UserResponse user = getUser(userId);

        // 사용자의 담당 hubId와 업체의 관리 hubId가 같아야 해당 업체의 상품을 생성할 수 있다.
        if (user.hubId() == null
                || !companyHubId.equals(user.hubId())) {

            throw new ApiException(
                    ProductErrorCode.PRODUCT_ACCESS_DENIED
            );
        }
    }


    private void validateCompanyId(UUID requestedCompanyId, CompanyResponse company) {

        if (company == null || company.companyId() == null) {
            throw new ApiException(ProductErrorCode.COMPANY_NOT_FOUND);
        }

        if (!requestedCompanyId.equals(company.companyId())) {
            throw new ApiException(ProductErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private void validateProducerCompany(CompanyResponse company) {

        if (company.companyType() != CompanyType.PRODUCER) {
            throw new ApiException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
    }

    private void validateCompanyHub(CompanyResponse company) {

        if (company.hubId() == null) {
            throw new ApiException(ProductErrorCode.COMPANY_HUB_NOT_FOUND);
        }
    }

    // 사용자 조회
    private UserResponse getUser(UUID userId) {
        UserResponse user = userClient
                .getUser(userId)
                .data();

        if (user == null || user.userId() == null) {
            throw new ApiException(
                    ProductErrorCode.PRODUCT_ACCESS_DENIED
            );
        }

        return user;
    }

}
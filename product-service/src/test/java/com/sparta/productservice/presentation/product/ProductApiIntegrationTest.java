package com.sparta.productservice.presentation.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.productservice.presentation.product.request.ProductCreateRequest;
import com.sparta.productservice.presentation.product.request.ProductUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.domain.inventory.Inventory;
import com.sparta.productservice.domain.inventory.InventoryRepository;
import com.sparta.productservice.domain.product.Product;
import com.sparta.productservice.domain.product.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * TODO: company-service 연동 완료 후 상품 생성 시 업체 존재 여부 검증 테스트를
 *       ProductCompanyIntegrationTest로 분리하여 작성한다.
 *
 * TODO: Gateway/JWT 인증·인가 적용 후 역할별 접근 제어 테스트를
 *       ProductAuthorizationTest로 분리하여 작성한다.
 *
 * TODO: FeignClient 통신 실패, 타임아웃, 404 응답 처리 테스트를
 *       CompanyClientTest로 분리하여 작성한다.
 *
 * TODO: 상품 삭제 시 주문 등 연관 서비스 비활성화 처리가 확정되면
 *       서비스 간 통합 테스트를 별도로 작성한다.
 */

/**
 * 상품 API 통합 테스트.
 *
 * MockMvc를 사용하여 Controller, Service, Repository,
 * Querydsl 검색 구현체, H2 DB, 예외 처리 흐름을 함께 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("상품 API 통합 테스트")
class ProductApiIntegrationTest {

    private static final String BASE_URL = "/api/v1/products";

    private static final UUID COMPANY_A_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID COMPANY_B_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Nested
    @DisplayName("상품 생성")
    class CreateProduct {

        @Test
        @DisplayName("상품을 등록할 수 있다")
        void createProduct_success() throws Exception {
            // given
            ProductCreateRequest request =
                    new ProductCreateRequest(COMPANY_A_ID, "테스트 상품");

            // when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("상품이 생성되었습니다."))
                    .andExpect(jsonPath("$.data.productId").exists())
                    .andExpect(jsonPath("$.data.companyId").value(COMPANY_A_ID.toString()))
                    .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Nested
    @DisplayName("상품 단건 조회")
    class GetProduct {

        @Test
        @DisplayName("등록된 상품을 ID로 조회할 수 있다")
        void getProduct_success() throws Exception {
            // given
            UUID productId = createProduct(COMPANY_A_ID, "서울 사과 1kg");

            // when & then
            mockMvc.perform(get(BASE_URL + "/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("상품이 조회되었습니다."))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.companyId").value(COMPANY_A_ID.toString()))
                    .andExpect(jsonPath("$.data.name").value("서울 사과 1kg"))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("존재하지 않는 상품을 조회하면 404를 반환한다")
        void getProduct_notFound() throws Exception {
            // given
            UUID unknownProductId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get(BASE_URL + "/{productId}", unknownProductId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("상품명을 수정할 수 있다")
        void updateProduct_success() throws Exception {
            // given
            UUID productId = createProduct(COMPANY_A_ID, "수정 전 상품명");
            ProductUpdateRequest request = new ProductUpdateRequest("수정 후 상품명");

            // when & then
            mockMvc.perform(patch(BASE_URL + "/{productId}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("상품이 수정되었습니다."))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.name").value("수정 후 상품명"));

            /*
             * 수정 응답만 확인하지 않고 다시 조회하여
             * 실제 DB에도 변경 내용이 반영되었는지 검증한다.
             */
            mockMvc.perform(get(BASE_URL + "/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정 후 상품명"));
        }
    }

    @Nested
    @DisplayName("상품 목록 및 검색")
    class SearchProducts {

        @Test
        @DisplayName("상품명의 일부 문자열로 상품을 검색할 수 있다")
        void searchProducts_byName() throws Exception {
            // given
            createProduct(COMPANY_A_ID, "서울 사과 1kg");
            createProduct(COMPANY_A_ID, "서울 배 1kg");
            createProduct(COMPANY_B_ID, "부산 고등어");

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("name", "서울")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[*].name", hasItem("서울 사과 1kg")))
                    .andExpect(jsonPath("$.data.content[*].name", hasItem("서울 배 1kg")))
                    .andExpect(jsonPath("$.data.content[*].name", not(hasItem("부산 고등어"))));
        }

        @Test
        @DisplayName("업체 ID로 해당 업체의 상품만 검색할 수 있다")
        void searchProducts_byCompanyId() throws Exception {
            // given
            createProduct(COMPANY_A_ID, "업체 A 상품 1");
            createProduct(COMPANY_A_ID, "업체 A 상품 2");
            createProduct(COMPANY_B_ID, "업체 B 상품 1");

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("companyId", COMPANY_A_ID.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath(
                            "$.data.content[*].companyId",
                            hasItem(COMPANY_A_ID.toString())
                    ))
                    .andExpect(jsonPath(
                            "$.data.content[*].companyId",
                            not(hasItem(COMPANY_B_ID.toString()))
                    ));
        }

        @Test
        @DisplayName("상품명과 업체 ID 검색 조건을 함께 사용할 수 있다")
        void searchProducts_byNameAndCompanyId() throws Exception {
            // given
            createProduct(COMPANY_A_ID, "서울 사과");
            createProduct(COMPANY_A_ID, "서울 배");
            createProduct(COMPANY_B_ID, "서울 사과");

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("name", "사과")
                            .param("companyId", COMPANY_A_ID.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].name").value("서울 사과"))
                    .andExpect(jsonPath("$.data.content[0].companyId")
                            .value(COMPANY_A_ID.toString()));
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class DeleteProduct {

        @Test
        @DisplayName("상품을 논리적으로 삭제할 수 있다")
        void deleteProduct_success() throws Exception {
            // given
            UUID productId = createProduct(COMPANY_A_ID, "삭제 대상 상품");

            // when & then
            mockMvc.perform(delete(BASE_URL + "/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("상품을 삭제하면 연결된 재고도 함께 논리 삭제된다")
        void deleteProduct_deletesInventoryTogether() throws Exception {
            // given
            /*
             * 상품 생성 API를 호출하면 ProductService 내부에서 초기 수량 0의 재고도 함께 생성된다.
             */
            UUID productId = createProduct(COMPANY_A_ID, "재고 동반 삭제 상품");

            /*
             * 상품 삭제 전에 연결된 활성 재고가 실제로 존재하는지 확인한다.
             * 이후 재고가 조회되지 않는 것이 상품 삭제 처리 때문임을
             * 명확하게 검증하기 위한 사전 조건이다.
             */
            Inventory inventoryBeforeDelete = inventoryRepository
                            .findByProductIdAndDeletedAtIsNull(productId)
                            .orElseThrow();

            UUID inventoryId = inventoryBeforeDelete.getId();

            assertThat(inventoryBeforeDelete.getDeletedAt()).isNull();

            // when
            mockMvc.perform(delete(BASE_URL + "/{productId}",
                                    productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            /*
             * 변경 감지로 발생하는 상품·재고 UPDATE 쿼리를
             * DB에 즉시 반영한다.
             */
            productRepository.flush();
            inventoryRepository.flush();

            // then 1. 삭제된 상품은 활성 상품 조회에서 제외된다.
            assertThat(productRepository.findByIdAndDeletedAtIsNull(productId)).isEmpty();

            // then 2. 삭제된 재고도 활성 재고 조회에서 제외된다.
            assertThat(inventoryRepository.findByProductIdAndDeletedAtIsNull(productId)).isEmpty();

            /*
             * 논리 삭제이므로 DB 행 자체는 남아 있어야 한다.
             * 삭제 조건이 없는 findById()로 다시 조회하여
             * deletedAt과 deletedBy가 기록됐는지 확인한다.
             */
            Product deletedProduct = productRepository.findById(productId).orElseThrow();
            Inventory deletedInventory = inventoryRepository.findById(inventoryId).orElseThrow();

            assertThat(deletedProduct.getDeletedAt()).isNotNull();
            assertThat(deletedInventory.getDeletedAt()).isNotNull();

            /*
             * 상품과 재고가 같은 삭제 요청에 의해 처리되므로
             * deletedBy도 동일해야 한다.
             */
            assertThat(deletedInventory.getDeletedBy()).isEqualTo(deletedProduct.getDeletedBy());
        }

        @Test
        @DisplayName("연결된 재고가 없는 상품도 정상적으로 논리 삭제할 수 있다")
        void deleteProduct_withoutInventory_success() throws Exception {
            // given
            /*
             * createProduct() 헬퍼는 상품 생성 API를 호출하므로
             * 초기 재고도 자동으로 생성된다.
             *
             * 따라서 재고가 없는 상품 데이터를 만들기 위해
             * Product 엔티티만 Repository로 직접 저장한다.
             */
            Product productWithoutInventory = Product.create(COMPANY_A_ID, "재고 없는 상품");

            Product savedProduct = productRepository.saveAndFlush(productWithoutInventory);

            UUID productId = savedProduct.getId();

            /*
             * 테스트 시작 시점에 연결된 재고가 존재하지 않는지 확인한다.
             */
            assertThat(inventoryRepository.findByProductIdAndDeletedAtIsNull(productId)).isEmpty();

            // when
            mockMvc.perform(delete(BASE_URL + "/{productId}",
                                    productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            productRepository.flush();

            // then 1. 재고가 없어도 상품은 활성 조회에서 제외된다.
            assertThat(productRepository.findByIdAndDeletedAtIsNull(productId)).isEmpty();

            // then 2. 상품 행에는 논리 삭제 정보가 기록된다.
            Product deletedProduct = productRepository.findById(productId).orElseThrow();

            assertThat(deletedProduct.getDeletedAt()).isNotNull();

            assertThat(deletedProduct.getDeletedBy()).isNotNull();

            /*
             * 상품 삭제 과정에서 존재하지 않던 재고가
             * 새로 생성되지 않았는지도 함께 확인한다.
             */
            assertThat(inventoryRepository.findByProductIdAndDeletedAtIsNull(productId)).isEmpty();
        }


        @Test
        @DisplayName("삭제된 상품을 단건 조회하면 404를 반환한다")
        void getDeletedProduct_notFound() throws Exception {
            // given
            UUID productId = createProduct(COMPANY_A_ID, "삭제 후 조회 상품");

            mockMvc.perform(delete(BASE_URL + "/{productId}", productId))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(get(BASE_URL + "/{productId}", productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("삭제된 상품은 목록과 검색 결과에서 제외된다")
        void searchProducts_excludesDeletedProduct() throws Exception {
            // given
            UUID activeProductId = createProduct(COMPANY_A_ID, "활성 상품");
            UUID deletedProductId = createProduct(COMPANY_A_ID, "삭제 상품");

            mockMvc.perform(delete(BASE_URL + "/{productId}", deletedProductId))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("companyId", COMPANY_A_ID.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath(
                            "$.data.content[*].productId",
                            hasItem(activeProductId.toString())
                    ))
                    .andExpect(jsonPath(
                            "$.data.content[*].productId",
                            not(hasItem(deletedProductId.toString()))
                    ));
        }
    }

    /**
     * 상품 생성 API를 실제로 호출하고,
     * 응답에서 생성된 productId를 추출한다.
     */
    private UUID createProduct(UUID companyId, String name) throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(companyId, name);

        String responseBody = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        String productId = rootNode.path("data").path("productId").asText();

        return UUID.fromString(productId);
    }
}
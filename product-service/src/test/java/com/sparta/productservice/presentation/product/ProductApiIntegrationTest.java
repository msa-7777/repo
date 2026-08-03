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
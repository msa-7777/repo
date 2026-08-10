package com.sparta.productservice.presentation.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.productservice.domain.inventory.Inventory;
import com.sparta.productservice.domain.inventory.InventoryRepository;
import com.sparta.productservice.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 재고 API 통합 테스트.
/*
* MockMvc를 이용하여 Controller부터 Service, Repository, H2 DB까지 실제 애플리케이션 흐름을 함께 검증한다.
*
* 검증 범위:
* 1. 상품 생성 시 초기 재고 자동 생성
* 2. 재고 단건 조회
* 3. 재고 수량 증가·차감·복구
* 4. 재고 부족 및 잘못된 요청 검증
* 5. 재고 목록·검색·페이징 검증
*/
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("재고 API 통합 테스트")
class InventoryApiIntegrationTest {

    /**
     * ProductService에서 로컬 테스트용으로 사용하는 임시 허브 ID와 반드시 같은 값이어야 한다.
     *
     * TODO: company-service 연동 후에는 업체 단건 조회 응답의 hubId를 검증하도록 수정한다.
     */
    private static final UUID TEMPORARY_HUB_ID =
            UUID.fromString(
                    "f6a7b8c9-0000-0000-0000-000000000001"
            );

    private static final UUID SECOND_HUB_ID =
            UUID.fromString(
                    "f6a7b8c9-0000-0000-0000-000000000002"
            );

    private static final UUID COMPANY_ID =
            UUID.fromString(
                    "a1b2c3d4-0000-0000-0000-000000000001"
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 각 테스트가 서로 영향을 주지 않도록 테스트 실행 전에 DB 데이터를 비운다.
     *
     * Inventory가 Product의 ID를 참조하므로 재고 데이터를 먼저 삭제하고 상품 데이터를 삭제한다.
     */
    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("상품 생성 시 초기 재고 생성")
    class ProductCreation {

        @Test
        @DisplayName("상품을 생성하면 수량 0의 초기 재고도 함께 생성된다")
        void createProduct_createsInitialInventory() throws Exception {
            // given
            String requestBody = """
                    {
                      "companyId": "%s",
                      "name": "초기 재고 생성 테스트 상품"
                    }
                    """.formatted(COMPANY_ID);

            // when
            String responseBody = mockMvc.perform(
                            post("/api/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(
                            jsonPath("$.data.productId").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            /*
             * 상품 생성 응답에서 productId를 추출한다.
             * 이후 해당 productId와 연결된 재고가 생성됐는지 확인한다.
             */
            JsonNode root = objectMapper.readTree(responseBody);

            UUID productId = UUID.fromString(
                    root.path("data")
                            .path("productId")
                            .asText()
            );

            // then
            Inventory inventory = inventoryRepository
                    .findByProductIdAndDeletedAtIsNull(productId)
                    .orElseThrow();

            assertThat(inventory.getProductId())
                    .isEqualTo(productId);

            assertThat(inventory.getHubId())
                    .isEqualTo(TEMPORARY_HUB_ID);

            assertThat(inventory.getQuantity())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("재고 단건 조회")
    class GetInventory {

        @Test
        @DisplayName("상품 ID로 활성 재고를 조회할 수 있다")
        void getInventory_success() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    50
            );

            // when & then
            mockMvc.perform(
                            get("/api/v1/inventories/{productId}",
                                inventory.getProductId()
                            )
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.inventoryId")
                            .value(inventory.getId().toString())
                    )
                    .andExpect(jsonPath("$.data.productId")
                                    .value(inventory.getProductId().toString())
                    )
                    .andExpect(jsonPath("$.data.hubId")
                                    .value(TEMPORARY_HUB_ID.toString())
                    )
                    .andExpect(jsonPath("$.data.quantity")
                                    .value(50)
                    );
        }

        @Test
        @DisplayName("존재하지 않는 상품의 재고를 조회하면 404를 반환한다")
        void getInventory_notFound() throws Exception {
            // given
            UUID unknownProductId = UUID.randomUUID();

            // when & then
            mockMvc.perform(
                            get("/api/v1/inventories/{productId}",
                                unknownProductId
                            )
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(
                            jsonPath("$.error")
                                    .value("INVENTORY_NOT_FOUND")
                    );
        }
    }

    @Nested
    @DisplayName("재고 수량 변경")
    class ChangeQuantity {

        @Test
        @DisplayName("INBOUND 요청은 재고를 증가시킨다")
        void inbound_increasesQuantity() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    0
            );

            String requestBody = """
                    {
                      "changeType": "INBOUND",
                      "quantity": 100
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/inventories/{productId}/quantity",
                                  inventory.getProductId()
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(
                            jsonPath("$.data.quantity")
                                    .value(100));

            /*
             * 응답만 확인하지 않고 DB를 다시 조회해
             * 실제 데이터에도 수량이 반영됐는지 확인한다.
             */
            Inventory updatedInventory = findInventory(inventory.getProductId());

            assertThat(updatedInventory.getQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("ORDER_DECREASE 요청은 재고를 차감한다")
        void orderDecrease_decreasesQuantity() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    100
            );

            String requestBody = """
                    {
                      "changeType": "ORDER_DECREASE",
                      "quantity": 30
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/inventories/{productId}/quantity",
                                  inventory.getProductId()
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.quantity").value(70));

            Inventory updatedInventory = findInventory(inventory.getProductId());

            assertThat(updatedInventory.getQuantity()).isEqualTo(70);
        }

        @Test
        @DisplayName("ORDER_CANCEL_RESTORE 요청은 재고를 복구한다")
        void orderCancelRestore_increasesQuantity() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    70
            );

            String requestBody = """
                    {
                      "changeType": "ORDER_CANCEL_RESTORE",
                      "quantity": 10
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/inventories/{productId}/quantity",
                                  inventory.getProductId()
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.quantity").value(80));

            Inventory updatedInventory = findInventory(inventory.getProductId());

            assertThat(updatedInventory.getQuantity()).isEqualTo(80);
        }

        @Test
        @DisplayName("현재 수량보다 많이 차감하면 409를 반환하고 수량은 유지된다")
        void orderDecrease_insufficientInventory() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    20
            );

            String requestBody = """
                    {
                      "changeType": "ORDER_DECREASE",
                      "quantity": 30
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/inventories/{productId}/quantity",
                                  inventory.getProductId()
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.error")
                                    .value("INSUFFICIENT_INVENTORY"));

            /*
             * 예외가 발생한 경우 트랜잭션이 롤백되어
             * 기존 수량 20이 그대로 유지되어야 한다.
             */
            Inventory unchangedInventory = findInventory(inventory.getProductId());

            assertThat(unchangedInventory.getQuantity()).isEqualTo(20);
        }

        @Test
        @DisplayName("변경 수량이 0이면 400을 반환한다")
        void changeQuantity_zeroQuantity() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    10
            );

            String requestBody = """
                    {
                      "changeType": "INBOUND",
                      "quantity": 0
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/inventories/{productId}/quantity",
                                  inventory.getProductId()
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            Inventory unchangedInventory = findInventory(inventory.getProductId());

            assertThat(unchangedInventory.getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("changeType이 없으면 400을 반환한다")
        void changeQuantity_missingChangeType() throws Exception {
            // given
            Inventory inventory = saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    10
            );

            String requestBody = """
                    {
                      "quantity": 5
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/inventories/{productId}/quantity",
                                  inventory.getProductId()
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("재고 목록 및 검색")
    class SearchInventories {

        @Test
        @DisplayName("삭제되지 않은 재고 목록을 페이징하여 조회한다")
        void getInventories_success() throws Exception {
            // given
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    10
            );
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    20
            );
            saveInventory(
                    UUID.randomUUID(),
                    SECOND_HUB_ID,
                    30
            );

            // when & then
            mockMvc.perform(
                            get("/api/v1/inventories")
                                    .param("page", "0")
                                    .param("size", "2")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(3))
                    .andExpect(jsonPath("$.data.totalPages").value(2));
        }

        @Test
        @DisplayName("hubId로 재고를 검색할 수 있다")
        void searchInventories_byHubId() throws Exception {
            // given
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    10
            );
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    20
            );
            saveInventory(
                    UUID.randomUUID(),
                    SECOND_HUB_ID,
                    30
            );

            // when & then
            mockMvc.perform(
                            get("/api/v1/inventories")
                                    .param("hubId", TEMPORARY_HUB_ID.toString())
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()")
                                    .value(2)
                    )
                    .andExpect(jsonPath("$.data.totalElements")
                                    .value(2)
                    )
                    .andExpect(jsonPath(
                                    "$.data.content[*].hubId")
                                    .value(org.hamcrest.Matchers
                                                    .everyItem(org.hamcrest.Matchers.is(
                                                                            TEMPORARY_HUB_ID.toString()
                                                                    )
                                                    )
                                    )
                    );
        }

        @Test
        @DisplayName("최소 수량과 최대 수량으로 재고를 검색할 수 있다")
        void searchInventories_byQuantityRange() throws Exception {
            // given
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    5
            );
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    20
            );
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    50
            );
            saveInventory(
                    UUID.randomUUID(),
                    TEMPORARY_HUB_ID,
                    100
            );

            // when & then
            mockMvc.perform(
                            get("/api/v1/inventories")
                                    .param("minQuantity", "20")
                                    .param("maxQuantity", "50")
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.content.length()")
                                    .value(2)
                    )
                    .andExpect(
                            jsonPath("$.data.totalElements")
                                    .value(2)
                    );
        }

        @Test
        @DisplayName("최소 수량이 최대 수량보다 크면 400을 반환한다")
        void searchInventories_invalidQuantityRange() throws Exception {
            // when & then
            mockMvc.perform(
                            get("/api/v1/inventories")
                                    .param("minQuantity", "100")
                                    .param("maxQuantity", "10")
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(
                            jsonPath("$.error")
                                    .value(
                                            "INVALID_QUANTITY_RANGE"
                                    )
                    );
        }
    }

    /**
     * 테스트용 재고 데이터를 저장한다.
     *
     * Inventory.create()는 초기 수량을 0으로 생성하므로,
     * 원하는 초기 수량이 있으면 increase()를 호출해 설정한다.
     */
    private Inventory saveInventory(
            UUID productId,
            UUID hubId,
            int quantity
    ) {
        Inventory inventory = Inventory.create(productId, hubId);

        if (quantity > 0) {
            inventory.increase(quantity);
        }

        return inventoryRepository.saveAndFlush(inventory);
    }

    /**
     * 상품 ID를 기준으로 활성 재고를 다시 조회한다.
     *
     * API 호출 이후 실제 DB 반영 결과를 검증할 때 사용한다.
     */
    private Inventory findInventory(UUID productId) {
        return inventoryRepository
                .findByProductIdAndDeletedAtIsNull(productId)
                .orElseThrow();
    }
}
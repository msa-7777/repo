package com.msa7.company.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa7.company.domain.model.Company;
import com.msa7.company.domain.model.CompanyType;
import com.msa7.company.domain.repository.CompanyRepository;
import com.msa7.company.presentation.dto.request.CreateCompanyRequest;
import com.msa7.company.presentation.dto.request.UpdateCompanyRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompanyApiIntegrationTest {

    private static final String BASE_URL = "/api/v1/companies";
    private static final UUID HUB_A_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID HUB_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Nested
    @DisplayName("업체 등록 (Create)")
    class CreateCompany {

        @Test
        @DisplayName("성공: 올바른 요청 데이터로 업체를 등록할 수 있다")
        void createCompany_success() throws Exception {
            CreateCompanyRequest request = new CreateCompanyRequest(
                    "(주) 테스트 물류",
                    CompanyType.PRODUCER,
                    HUB_A_ID,
                    "서울특별시 강남구 테헤란로 123"
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("업체가 성공적으로 등록되었습니다."))
                    .andExpect(jsonPath("$.data.companyId").exists())
                    .andExpect(jsonPath("$.data.name").value("(주) 테스트 물류"))
                    .andExpect(jsonPath("$.data.type").value("PRODUCER"))
                    .andExpect(jsonPath("$.data.hubId").value(HUB_A_ID.toString()))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        @Test
        @DisplayName("실패: 중복된 업체 이름으로 등록 시 오류가 발생한다")
        void createCompany_duplicateName() throws Exception {
            createCompanyHelper("(주) 중복물류", CompanyType.PRODUCER, HUB_A_ID);
            CreateCompanyRequest duplicateRequest = new CreateCompanyRequest(
                    "(주) 중복물류",
                    CompanyType.RECEIVER,
                    HUB_B_ID,
                    "경기도 성남시 분당구 판교로 456"
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("DUPLICATE_COMPANY_NAME"));
        }
    }

    @Nested
    @DisplayName("업체 단건 조회 (Get)")
    class GetCompany {

        @Test
        @DisplayName("성공: 업체 ID로 업체를 단건 조회할 수 있다")
        void getCompany_success() throws Exception {
            UUID companyId = createCompanyHelper("(주) 조회업체", CompanyType.PRODUCER, HUB_A_ID);

            mockMvc.perform(get(BASE_URL + "/{companyId}", companyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("업체 정보 조회가 완료되었습니다."))
                    .andExpect(jsonPath("$.data.companyId").value(companyId.toString()))
                    .andExpect(jsonPath("$.data.name").value("(주) 조회업체"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 업체 ID 조회 시 404를 반환한다")
        void getCompany_notFound() throws Exception {
            UUID unknownId = UUID.randomUUID();

            mockMvc.perform(get(BASE_URL + "/{companyId}", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("COMPANY_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("업체 목록 동적 검색 (Search - QueryDSL)")
    class SearchCompanies {

        @Test
        @DisplayName("성공: 업체명 키워드로 검색할 수 있다")
        void searchCompanies_byName() throws Exception {
            createCompanyHelper("한국물류", CompanyType.PRODUCER, HUB_A_ID);
            createCompanyHelper("한국유통", CompanyType.RECEIVER, HUB_A_ID);
            createCompanyHelper("서울상사", CompanyType.PRODUCER, HUB_B_ID);

            mockMvc.perform(get(BASE_URL)
                            .param("name", "한국")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[*].name", hasItem("한국물류")))
                    .andExpect(jsonPath("$.data.content[*].name", hasItem("한국유통")))
                    .andExpect(jsonPath("$.data.content[*].name", not(hasItem("서울상사"))));
        }

        @Test
        @DisplayName("성공: 업체 타입 및 허브 ID 조건으로 검색할 수 있다")
        void searchCompanies_byTypeAndHubId() throws Exception {
            createCompanyHelper("업체1", CompanyType.PRODUCER, HUB_A_ID);
            createCompanyHelper("업체2", CompanyType.RECEIVER, HUB_A_ID);
            createCompanyHelper("업체3", CompanyType.PRODUCER, HUB_B_ID);

            mockMvc.perform(get(BASE_URL)
                            .param("type", "PRODUCER")
                            .param("hubId", HUB_A_ID.toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].name").value("업체1"));
        }
    }

    @Nested
    @DisplayName("업체 수정 (Update)")
    class UpdateCompany {

        @Test
        @DisplayName("성공: 업체 정보를 수정할 수 있다")
        void updateCompany_success() throws Exception {
            UUID companyId = createCompanyHelper("기존업체", CompanyType.PRODUCER, HUB_A_ID);
            UpdateCompanyRequest updateRequest = new UpdateCompanyRequest(
                    "수정된업체",
                    CompanyType.RECEIVER,
                    HUB_B_ID,
                    "서울특별시 마포구 월드컵북로 999"
            );

            mockMvc.perform(patch(BASE_URL + "/{companyId}", companyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정된업체"))
                    .andExpect(jsonPath("$.data.type").value("RECEIVER"))
                    .andExpect(jsonPath("$.data.hubId").value(HUB_B_ID.toString()));
        }
    }

    @Nested
    @DisplayName("업체 삭제 (Delete)")
    class DeleteCompany {

        @Test
        @DisplayName("성공: 업체를 삭제하면 Soft Delete(deletedAt 세팅) 처리된다")
        void deleteCompany_success() throws Exception {
            UUID companyId = createCompanyHelper("삭제할업체", CompanyType.PRODUCER, HUB_A_ID);

            mockMvc.perform(delete(BASE_URL + "/{companyId}", companyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("업체가 성공적으로 삭제되었습니다."));

            companyRepository.flush();

            mockMvc.perform(get(BASE_URL + "/{companyId}", companyId))
                    .andExpect(status().isNotFound());

            Company deletedCompany = companyRepository.findById(companyId).orElseThrow();
            assertThat(deletedCompany.getDeletedAt()).isNotNull();
        }
    }

    // 헬퍼 메서드
    private UUID createCompanyHelper(String name, CompanyType type, UUID hubId) throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest(
                name,
                type,
                hubId,
                "서울특별시 강남구 테헤란로 100"
        );

        String responseBody = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // isOk() -> isCreated() 로 수정
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);
        String companyIdStr = rootNode.path("data").path("companyId").asText();
        return UUID.fromString(companyIdStr);
    }
}
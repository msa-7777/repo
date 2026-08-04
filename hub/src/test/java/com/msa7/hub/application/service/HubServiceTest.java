package com.msa7.hub.application.service;

import com.msa7.hub.domain.exception.BusinessException;
import com.msa7.hub.domain.exception.ErrorCode;
import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.repository.HubRepository;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.response.HubResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class HubServiceTest {

    @InjectMocks
    private HubService hubService;

    @Mock
    private HubRepository hubRepository;

    @Nested
    @DisplayName("허브 생성")
    class CreateHub {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID centralHubId = UUID.randomUUID();
            Hub centralHub = Hub.createHub(null, "중앙허브", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "서울시 중구");
            HubRequest request = createHubRequest("이름", "주소", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), centralHubId);

            Hub savedHub = Hub.createHub(centralHubId, request.name(), request.latitude(), request.longitude(), request.address());
            ReflectionTestUtils.setField(savedHub, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(savedHub, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(savedHub, "updatedAt", LocalDateTime.now());

            when(hubRepository.findByIdAndDeletedAtIsNull(centralHubId)).thenReturn(Optional.of(centralHub));
            when(hubRepository.save(any(Hub.class))).thenReturn(savedHub);

            // when
            HubResponse response = hubService.createHub(request);

            // then
            verify(hubRepository).findByIdAndDeletedAtIsNull(centralHubId);
            verify(hubRepository).save(any(Hub.class));

            assertThat(response.hubId()).isEqualTo(savedHub.getId());
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.centralHubId()).isEqualTo(centralHubId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 중앙 허브")
        void centralHubNotFound() {

            // given
            UUID centralHubId = UUID.randomUUID();
            HubRequest request = createHubRequest("이름", "주소", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), centralHubId);
            when(hubRepository.findByIdAndDeletedAtIsNull(centralHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.createHub(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CENTRAL_HUB_NOT_FOUND);

        }
    }

    @Nested
    @DisplayName("허브 수정")
    class UpdateHub {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID oldCentralHubId = UUID.randomUUID();
            UUID newCentralHubId = UUID.randomUUID();
            Hub newCentralHub = Hub.createHub(null, "새 중앙허브", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "서울시 중구");
            HubRequest request = createHubRequest("이름2", "주소2", BigDecimal.valueOf(35.1), BigDecimal.valueOf(129.0), newCentralHubId);

            UUID hubId = UUID.randomUUID();
            Hub savedHub = Hub.createHub(oldCentralHubId, "이름", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "부산");

            ReflectionTestUtils.setField(savedHub, "id", hubId);
            ReflectionTestUtils.setField(savedHub, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(savedHub, "updatedAt", LocalDateTime.now());


            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(savedHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(newCentralHubId)).thenReturn(Optional.of(newCentralHub));
            when(hubRepository.saveAndFlush(any(Hub.class))).thenReturn(savedHub);

            // when
            HubResponse response = hubService.updateHub(hubId, request);

            // then
            assertThat(response.hubId()).isEqualTo(hubId);
            assertThat(response.name()).isEqualTo("이름2");
            assertThat(response.address()).isEqualTo("주소2");
            assertThat(response.latitude()).isEqualTo(BigDecimal.valueOf(35.1));
            assertThat(response.longitude()).isEqualTo(BigDecimal.valueOf(129.0));
            assertThat(response.centralHubId()).isEqualTo(newCentralHubId);

            verify(hubRepository).findByIdAndDeletedAtIsNull(hubId);
            verify(hubRepository).findByIdAndDeletedAtIsNull(newCentralHubId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 허브")
        void hubNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID centralHubId = UUID.randomUUID();
            HubRequest request = createHubRequest("이름2", "주소2", BigDecimal.valueOf(35.1), BigDecimal.valueOf(129.0), centralHubId);

            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.updateHub(hubId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);


        }

        @Test
        @DisplayName("실패 - 존재하지 않는 중앙 허브")
        void centralHubNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID centralHubId = UUID.randomUUID();
            Hub existingHub = Hub.createHub(null, "이름", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "부산");
            ReflectionTestUtils.setField(existingHub, "id", hubId);

            HubRequest request = createHubRequest("이름2", "주소2", BigDecimal.valueOf(35.1), BigDecimal.valueOf(129.0), centralHubId);

            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(existingHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(centralHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.updateHub(hubId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CENTRAL_HUB_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("허브 삭제")
    class DeleteHub {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Hub existingHub = Hub.createHub(null, "이름", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "부산");
            ReflectionTestUtils.setField(existingHub, "id", hubId);

            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(existingHub));

            // when
            hubService.deleteHub(hubId, userId);

            // then
            verify(hubRepository).findByIdAndDeletedAtIsNull(hubId);
            assertThat(existingHub.getDeletedAt()).isNotNull();
            assertThat(existingHub.getDeletedBy()).isEqualTo(userId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 허브")
        void hubNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.deleteHub(hubId, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("허브 단건 조회")
    class GetHub {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID hubId = UUID.randomUUID();
            Hub hub = Hub.createHub(null, "이름", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "부산");
            ReflectionTestUtils.setField(hub, "id", hubId);
            ReflectionTestUtils.setField(hub, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(hub, "updatedAt", LocalDateTime.now());

            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(hub));

            // when
            HubResponse response = hubService.getHub(hubId);

            // then
            verify(hubRepository).findByIdAndDeletedAtIsNull(hubId);
            assertThat(response.hubId()).isEqualTo(hubId);
            assertThat(response.name()).isEqualTo("이름");
            assertThat(response.latitude()).isEqualTo(BigDecimal.valueOf(37.5));
            assertThat(response.longitude()).isEqualTo(BigDecimal.valueOf(127.0));
            assertThat(response.address()).isEqualTo("부산");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 허브")
        void hubNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.getHub(hubId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);
        }


    }


    private HubRequest createHubRequest(String name, String address, BigDecimal latitude, BigDecimal longitude, UUID centralHubId) {
        return new HubRequest(name, address, latitude, longitude, centralHubId);
    }
}
package com.msa7.hub.application.service;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.infrastructure.persistence.HubRepository;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.request.HubSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
            Hub response = hubService.createHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());

            // then
            verify(hubRepository).findByIdAndDeletedAtIsNull(centralHubId);
            verify(hubRepository).save(any(Hub.class));

            assertThat(response.getId()).isEqualTo(savedHub.getId());
            assertThat(response.getName()).isEqualTo(request.name());
            assertThat(response.getCentralHubId()).isEqualTo(centralHubId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 중앙 허브")
        void centralHubNotFound() {

            // given
            UUID centralHubId = UUID.randomUUID();
            HubRequest request = createHubRequest("이름", "주소", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), centralHubId);
            when(hubRepository.findByIdAndDeletedAtIsNull(centralHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.createHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address()))
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
            when(hubRepository.save(any(Hub.class))).thenReturn(savedHub);

            // when
            Hub response = hubService.updateHub(hubId, request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());

            // then
            assertThat(response.getId()).isEqualTo(hubId);
            assertThat(response.getName()).isEqualTo("이름2");
            assertThat(response.getAddress()).isEqualTo("주소2");
            assertThat(response.getLatitude()).isEqualTo(BigDecimal.valueOf(35.1));
            assertThat(response.getLongitude()).isEqualTo(BigDecimal.valueOf(129.0));
            assertThat(response.getCentralHubId()).isEqualTo(newCentralHubId);

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
            assertThatThrownBy(() -> hubService.updateHub(hubId, request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address()))
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
            assertThatThrownBy(() -> hubService.updateHub(hubId, request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CENTRAL_HUB_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("허브 삭제 (softDelete)")
    class SoftDelete {
        @Test
        @DisplayName("성공 - deletedAt/deletedBy 세팅 후 저장")
        void success() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Hub existingHub = Hub.createHub(null, "이름", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "부산");
            ReflectionTestUtils.setField(existingHub, "id", hubId);

            // when
            hubService.softDelete(existingHub, userId);

            // then
            assertThat(existingHub.getDeletedAt()).isNotNull();
            assertThat(existingHub.getDeletedBy()).isEqualTo(userId);
            verify(hubRepository).save(existingHub);
        }

        @Test
        @DisplayName("실패 - 다른 허브가 중앙 허브로 참조 중이면 삭제 불가")
        void fail_referencedAsCentralHub() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Hub existingHub = Hub.createHub(null, "이름", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "부산");
            ReflectionTestUtils.setField(existingHub, "id", hubId);

            when(hubRepository.existsByCentralHubIdAndDeletedAtIsNull(hubId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> hubService.softDelete(existingHub, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_REFERENCED_BY_CHILD_HUB);

            assertThat(existingHub.getDeletedAt()).isNull();
            verify(hubRepository, never()).save(any());
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
            Hub response = hubService.getHub(hubId);

            // then
            verify(hubRepository).findByIdAndDeletedAtIsNull(hubId);
            assertThat(response.getId()).isEqualTo(hubId);
            assertThat(response.getName()).isEqualTo("이름");
            assertThat(response.getLatitude()).isEqualTo(BigDecimal.valueOf(37.5));
            assertThat(response.getLongitude()).isEqualTo(BigDecimal.valueOf(127.0));
            assertThat(response.getAddress()).isEqualTo("부산");
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

    @Nested
    @DisplayName("허브 목록 조회")
    class GetHubList {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            String name = "이름";
            String address = "주소";
            Boolean isCentral = true;
            Pageable pageable = PageRequest.of(0, 10);

            Hub hub = Hub.createHub(null, name, BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), address);
            ReflectionTestUtils.setField(hub, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(hub, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(hub, "updatedAt", LocalDateTime.now());

            Page<Hub> hubPage = new PageImpl<>(List.of(hub), pageable, 1);
            HubSearchRequest request = new HubSearchRequest(name, address, isCentral);

            when(hubRepository.search(name, address, isCentral, pageable)).thenReturn(hubPage);

            // when
            Page<Hub> response = hubService.getHubList(request.name(), request.address(), request.isCentral(), pageable);

            // then
            verify(hubRepository).search(name, address, isCentral, pageable);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getName()).isEqualTo(name);
            assertThat(response.getContent().get(0).getAddress()).isEqualTo(address);
        }

    }


    private HubRequest createHubRequest(String name, String address, BigDecimal latitude, BigDecimal longitude, UUID centralHubId) {
        return new HubRequest(name, address, latitude, longitude, centralHubId);
    }
}
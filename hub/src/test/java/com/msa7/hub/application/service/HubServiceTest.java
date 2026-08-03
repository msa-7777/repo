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

            when(hubRepository.findById(centralHubId)).thenReturn(Optional.of(centralHub));
            when(hubRepository.save(any(Hub.class))).thenReturn(savedHub);

            // when
            HubResponse response = hubService.createHub(request);

            // then
            verify(hubRepository).findById(centralHubId);
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
            when(hubRepository.findById(centralHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.createHub(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CENTRAL_HUB_NOT_FOUND);

        }
    }

    private HubRequest createHubRequest(String name, String address, BigDecimal latitude, BigDecimal longitude, UUID centralHubId) {
        return new HubRequest(name, address, latitude, longitude, centralHubId);
    }
}
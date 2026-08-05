package com.msa7.hub.application.service;

import com.msa7.hub.domain.exception.BusinessException;
import com.msa7.hub.domain.exception.ErrorCode;
import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.domain.repository.HubRepository;
import com.msa7.hub.domain.repository.HubRouteRepository;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.response.HubRouteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubRouteServiceTest {

    @InjectMocks
    private HubRouteService hubRouteService;

    @Mock
    private HubRouteRepository hubRouteRepository;

    @Mock
    private HubRepository hubRepository;

    private Hub hub(UUID id, UUID centralHubId, String name) {
        Hub hub = Hub.createHub(centralHubId, name, BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "주소");
        ReflectionTestUtils.setField(hub, "id", id);
        return hub;
    }

    private HubRoute withId(HubRoute hubRoute) {
        ReflectionTestUtils.setField(hubRoute, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(hubRoute, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(hubRoute, "updatedAt", LocalDateTime.now());
        return hubRoute;
    }

    @Nested
    @DisplayName("허브 경로 생성")
    class CreateHubRoute {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, null, "도착허브");

            HubRoute savedHubRoute = withId(HubRoute.createHubRoute(fromHub, toHub, request.duration(), request.distance()));

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId)).thenReturn(false);
            when(hubRouteRepository.saveAndFlush(any(HubRoute.class))).thenReturn(savedHubRoute);

            // when
            HubRouteResponse response = hubRouteService.createHubRoute(request);

            // then
            assertThat(response.fromHubId()).isEqualTo(fromHubId);
            assertThat(response.toHubId()).isEqualTo(toHubId);
            assertThat(response.distance()).isEqualTo(request.distance());
            assertThat(response.duration()).isEqualTo(request.duration());

            verify(hubRouteRepository).saveAndFlush(any(HubRoute.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 출발 허브")
        void fromHubNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);

            verify(hubRouteRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 도착 허브")
        void toHubNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = Hub.createHub(null, "출발허브", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "서울");

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);

            verify(hubRouteRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("실패 - 출발 허브와 도착 허브가 동일함")
        void sameHub() {
            // given
            UUID hubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(hubId, hubId, 100, 60);

            Hub hub = hub(hubId, null, "허브");

            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(hub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(hubId, hubId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SAME_HUB_ROUTE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("실패 - 이미 등록된 경로")
        void alreadyExists() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = Hub.createHub(null, "출발허브", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "서울");
            Hub toHub = Hub.createHub(null, "도착허브", BigDecimal.valueOf(35.1), BigDecimal.valueOf(129.0), "부산");

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_ROUTE_ALREADY_EXISTS);

            verify(hubRouteRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("성공 - 스포크와 자기 소속 중앙허브")
        void spokeToOwnCentralHub() {
            // given
            UUID centralHubId = UUID.randomUUID();
            UUID spokeHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(spokeHubId, centralHubId, 100, 60);

            Hub spokeHub = hub(spokeHubId, centralHubId, "스포크허브");
            Hub centralHub = hub(centralHubId, null, "중앙허브");

            HubRoute savedHubRoute = withId(HubRoute.createHubRoute(spokeHub, centralHub, request.duration(), request.distance()));

            when(hubRepository.findByIdAndDeletedAtIsNull(spokeHubId)).thenReturn(Optional.of(spokeHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(centralHubId)).thenReturn(Optional.of(centralHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(spokeHubId, centralHubId)).thenReturn(false);
            when(hubRouteRepository.saveAndFlush(any(HubRoute.class))).thenReturn(savedHubRoute);

            // when
            HubRouteResponse response = hubRouteService.createHubRoute(request);

            // then
            assertThat(response.fromHubId()).isEqualTo(spokeHubId);
            assertThat(response.toHubId()).isEqualTo(centralHubId);
        }

        @Test
        @DisplayName("실패 - 스포크끼리 연결 (중앙허브를 거치지 않음)")
        void spokeToSpoke() {
            // given
            UUID centralHubId = UUID.randomUUID();
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, centralHubId, "출발스포크");
            Hub toHub = hub(toHubId, centralHubId, "도착스포크");

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_HUB_ROUTE);

            verify(hubRouteRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("실패 - 스포크가 소속되지 않은 다른 중앙허브와 연결")
        void spokeToWrongCentralHub() {
            // given
            UUID ownCentralHubId = UUID.randomUUID();
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, ownCentralHubId, "스포크");
            Hub toHub = hub(toHubId, null, "다른중앙허브");

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_HUB_ROUTE);

            verify(hubRouteRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("실패 - 동시 요청으로 인한 유니크 제약 위반(레이스 컨디션)")
        void concurrentDuplicate() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, null, "도착허브");

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId)).thenReturn(false);
            when(hubRouteRepository.saveAndFlush(any(HubRoute.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint \"uq_hub_route_from_to\""));

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_ROUTE_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("허브 경로 수정")
    class UpdateHubRoute {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID hubRouteId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, null, "도착허브");

            HubRoute existingHubRoute = withId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
            ReflectionTestUtils.setField(existingHubRoute, "id", hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(existingHubRoute));
            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.saveAndFlush(any(HubRoute.class))).thenReturn(existingHubRoute);

            // when
            HubRouteResponse response = hubRouteService.updateHubRoute(hubRouteId, request);

            // then
            assertThat(response.fromHubId()).isEqualTo(fromHubId);
            assertThat(response.toHubId()).isEqualTo(toHubId);
            assertThat(response.distance()).isEqualTo(request.distance());
            assertThat(response.duration()).isEqualTo(request.duration());

            verify(hubRouteRepository).saveAndFlush(any(HubRoute.class));
        }
        @Test
        @DisplayName("실패 - 존재하지 않는 허브 라우트")
        void hubRouteNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID hubRouteId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.empty());
            // when & then
            assertThatThrownBy(() -> hubRouteService.updateHubRoute(hubRouteId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_ROUTE_NOT_FOUND);

            verify(hubRouteRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 출발 허브")
        void fromHubNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID hubRouteId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, null, "도착허브");

            HubRoute existingHubRoute = withId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
            ReflectionTestUtils.setField(existingHubRoute, "id", hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(existingHubRoute));
            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.empty());
            // when & then
            assertThatThrownBy(() -> hubRouteService.updateHubRoute(hubRouteId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 도착 허브")
        void toHubNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID hubRouteId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, null, "도착허브");

            HubRoute existingHubRoute = withId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
            ReflectionTestUtils.setField(existingHubRoute, "id", hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(existingHubRoute));
            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.updateHubRoute(hubRouteId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 스포크끼리 연결 (중앙허브를 거치지 않음)")
        void spokeToSpoke() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID hubRouteId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);

            UUID centralHubId = UUID.randomUUID();

            Hub fromHub = hub(fromHubId, centralHubId, "출발허브");
            Hub toHub = hub(toHubId, centralHubId, "도착허브");

            Hub existingFromHub = hub(UUID.randomUUID(), null, "기존 출발 허브");
            Hub existingToHub = hub(UUID.randomUUID(), null, "기존 도착 허브");
            HubRoute existingHubRoute = withId(HubRoute.createHubRoute(existingFromHub, existingToHub, 50, 30));
            ReflectionTestUtils.setField(existingHubRoute, "id", hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(existingHubRoute));
            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));

            // when & then
            assertThatThrownBy(() -> hubRouteService.updateHubRoute(hubRouteId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_HUB_ROUTE);
        }

        @Test
        @DisplayName("실패 - 소속 안된 다른 중앙허브로 변경")
        void spokeToWrongCentralHub() {

            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID hubRouteId = UUID.randomUUID();
            HubRouteRequest request = new HubRouteRequest(fromHubId, toHubId, 100, 60);


            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, UUID.randomUUID(), "도착허브");

            Hub existingFromHub = hub(UUID.randomUUID(), null, "기존 출발 허브");
            Hub existingToHub = hub(UUID.randomUUID(), null, "기존 도착 허브");
            HubRoute existingHubRoute = withId(HubRoute.createHubRoute(existingFromHub, existingToHub, 50, 30));
            ReflectionTestUtils.setField(existingHubRoute, "id", hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(existingHubRoute));
            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));

            // when & then
            assertThatThrownBy(() -> hubRouteService.updateHubRoute(hubRouteId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_HUB_ROUTE);
        }
    }

    @Nested
    @DisplayName("허브 경로 삭제 ")
    class DeleteHubRoute{
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID hubRouteId = UUID.randomUUID();
            UUID deletedBy = UUID.randomUUID();

            Hub fromHub = hub(UUID.randomUUID(), null, "출발허브");
            Hub toHub = hub(UUID.randomUUID(), null, "도착허브");
            HubRoute existingHubRoute = withId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
            ReflectionTestUtils.setField(existingHubRoute, "id", hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(existingHubRoute));

            // when
            hubRouteService.deleteHubRoute(hubRouteId, deletedBy);

            // then
            assertThat(existingHubRoute.getDeletedAt()).isNotNull();
            assertThat(existingHubRoute.getDeletedBy()).isEqualTo(deletedBy);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 허브 라우트")
        void hubRouteNotFound() {
            // given
            UUID hubRouteId = UUID.randomUUID();

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.deleteHubRoute(hubRouteId, UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_ROUTE_NOT_FOUND);
        }

    }

}

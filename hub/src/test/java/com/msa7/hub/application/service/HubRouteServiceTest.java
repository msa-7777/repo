package com.msa7.hub.application.service;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.domain.repository.HubRepository;
import com.msa7.hub.domain.repository.HubRouteRepository;
import com.msa7.hub.domain.repository.HubRouteSearchRepository;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.presentation.request.HubRoutePathRequest;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.request.HubRouteSearchRequest;
import com.msa7.hub.presentation.response.HubRoutePathResponse;
import com.msa7.hub.presentation.response.HubRouteResponse;
import com.msa7.hub.presentation.response.HubRouteSegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
class HubRouteServiceTest {

    @InjectMocks
    private HubRouteService hubRouteService;

    @Mock
    private HubRouteRepository hubRouteRepository;

    @Mock
    private HubRouteSearchRepository hubRouteSearchRepository;

    @Mock
    private HubRepository hubRepository;

    private Hub hub(UUID id, UUID centralHubId, String name) {
        Hub hub = Hub.createHub(centralHubId, name, BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), "주소");
        ReflectionTestUtils.setField(hub, "id", id);
        return hub;
    }

    private HubRoute hubRouteWithId(HubRoute hubRoute) {
        return hubRouteWithId(hubRoute, UUID.randomUUID());
    }

    private HubRoute hubRouteWithId(HubRoute hubRoute, UUID id) {
        ReflectionTestUtils.setField(hubRoute, "id", id);
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

            HubRoute savedHubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, request.duration(), request.distance()));

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)).thenReturn(false);
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
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(hubId, hubId)).thenReturn(false);

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
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)).thenReturn(true);

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

            HubRoute savedHubRoute = hubRouteWithId(HubRoute.createHubRoute(spokeHub, centralHub, request.duration(), request.distance()));

            when(hubRepository.findByIdAndDeletedAtIsNull(spokeHubId)).thenReturn(Optional.of(spokeHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(centralHubId)).thenReturn(Optional.of(centralHub));
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(spokeHubId, centralHubId)).thenReturn(false);
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
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)).thenReturn(false);

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
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)).thenReturn(false);

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
            when(hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)).thenReturn(false);
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

            HubRoute existingHubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
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

            HubRoute existingHubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
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

            HubRoute existingHubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
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
            HubRoute existingHubRoute = hubRouteWithId(HubRoute.createHubRoute(existingFromHub, existingToHub, 50, 30));
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
            HubRoute existingHubRoute = hubRouteWithId(HubRoute.createHubRoute(existingFromHub, existingToHub, 50, 30));
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
            HubRoute existingHubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));
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
    @Nested
    @DisplayName("허브 라우트 단건 조회")
    class GetHubRoute{
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID hubRouteId = UUID.randomUUID();
            Hub fromHub = hub(UUID.randomUUID(), null, "출발 허브");
            Hub toHub = hub(UUID.randomUUID(), null, "도착 허브");

            HubRoute hubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 50, 30), hubRouteId);

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.of(hubRoute));

            // when
            HubRouteResponse response = hubRouteService.getHubRoute(hubRouteId);

            // then
            assertThat(response.hubRouteId()).isEqualTo(hubRouteId);
            assertThat(response.fromHubId()).isEqualTo(hubRoute.getFromHubId());
            assertThat(response.toHubId()).isEqualTo(hubRoute.getToHubId());
            assertThat(response.distance()).isEqualTo(hubRoute.getDistance());
            assertThat(response.duration()).isEqualTo(hubRoute.getDuration());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 허브 라우트")
        void hubRouteNotFound() {
            // given
            UUID hubRouteId = UUID.randomUUID();

            when(hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.getHubRoute(hubRouteId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_ROUTE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("허브 라우트 목록 조회")
    class GetHubRouteList {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRouteSearchRequest request = new HubRouteSearchRequest(fromHubId, toHubId);
            Pageable pageable = PageRequest.of(0, 10);

            Hub fromHub = hub(fromHubId, null, "출발허브");
            Hub toHub = hub(toHubId, null, "도착허브");
            HubRoute hubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 50, 30));

            Page<HubRoute> hubRoutePage = new PageImpl<>(List.of(hubRoute), pageable, 1);

            when(hubRouteSearchRepository.search(fromHubId, toHubId, pageable)).thenReturn(hubRoutePage);

            // when
            Page<HubRouteResponse> response = hubRouteService.getHubRouteList(request, pageable);

            // then
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.getContent().get(0).fromHubId()).isEqualTo(fromHubId);
            assertThat(response.getContent().get(0).toHubId()).isEqualTo(toHubId);
        }
    }

    @Nested
    @DisplayName("허브 라우트 최적 경로 조회")
    class GetHubRoutePath {

        private void assertSegment(HubRouteSegment segment, int sequence, UUID fromId, UUID toId, int distance, int duration) {
            assertThat(segment.sequence()).isEqualTo(sequence);
            assertThat(segment.fromHubId()).isEqualTo(fromId);
            assertThat(segment.toHubId()).isEqualTo(toId);
            assertThat(segment.distance()).isEqualTo(distance);
            assertThat(segment.duration()).isEqualTo(duration);
        }

        @Test
        @DisplayName("성공 - spoke -> spoke(다른 중앙)")
        void spokeToSpoke() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID fromCentralId = UUID.randomUUID();

            UUID toHubId = UUID.randomUUID();
            UUID toCentralId = UUID.randomUUID();

            Hub fromHub = hub(fromHubId, fromCentralId, "출발 허브");
            Hub fromCentralHub = hub(fromCentralId, null, "출발 중앙 허브");
            Hub toHub = hub(toHubId, toCentralId, "도착 허브");
            Hub toCentralHub = hub(toCentralId, null, "도착 중앙 허브");

            HubRoutePathRequest request = new HubRoutePathRequest(fromHubId, toHubId);

            HubRoute hubRoute1= hubRouteWithId(HubRoute.createHubRoute(fromHub, fromCentralHub, 30, 30));
            HubRoute hubRoute2= hubRouteWithId(HubRoute.createHubRoute(fromCentralHub, toCentralHub, 40, 40));
            HubRoute hubRoute3= hubRouteWithId(HubRoute.createHubRoute(toCentralHub, toHub, 50, 50));

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));

            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, fromCentralId)).thenReturn(Optional.of(hubRoute1));
            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromCentralId, toCentralId)).thenReturn(Optional.of(hubRoute2));
            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(toCentralId, toHubId)).thenReturn(Optional.of(hubRoute3));

            // when
            HubRoutePathResponse response = hubRouteService.getHubRoutePath(request);

            // then
            assertThat(response.segments()).hasSize(3);

            assertSegment(response.segments().get(0), 0, fromHubId, fromCentralId, 30, 30);
            assertSegment(response.segments().get(1), 1, fromCentralId, toCentralId, 40, 40);
            assertSegment(response.segments().get(2), 2, toCentralId, toHubId, 50, 50);

            assertThat(response.totalDistance()).isEqualTo(120);
            assertThat(response.totalDuration()).isEqualTo(120);
        }

        @Test
        @DisplayName("성공 - spoke -> spoke(같은 중앙)")
        void spokeToSpoke_sameCentral() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID centralId = UUID.randomUUID();

            UUID toHubId = UUID.randomUUID();

            Hub fromHub = hub(fromHubId, centralId, "출발 허브");
            Hub centralHub = hub(centralId, null, "중앙 허브");
            Hub toHub = hub(toHubId, centralId, "도착 허브");

            HubRoutePathRequest request = new HubRoutePathRequest(fromHubId, toHubId);

            HubRoute hubRoute1= hubRouteWithId(HubRoute.createHubRoute(fromHub, centralHub, 30, 30));
            HubRoute hubRoute2= hubRouteWithId(HubRoute.createHubRoute(centralHub, toHub, 40, 40));

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));

            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, centralId)).thenReturn(Optional.of(hubRoute1));
            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(centralId, toHubId)).thenReturn(Optional.of(hubRoute2));

            // when
            HubRoutePathResponse response = hubRouteService.getHubRoutePath(request);

            // then
            assertThat(response.segments()).hasSize(2);

            assertSegment(response.segments().get(0), 0, fromHubId, centralId, 30, 30);
            assertSegment(response.segments().get(1), 1, centralId, toHubId, 40, 40);

            assertThat(response.totalDistance()).isEqualTo(70);
            assertThat(response.totalDuration()).isEqualTo(70);
        }

        @Test
        @DisplayName("성공 - 중앙 -> 중앙")
        void centralToCentral() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();

            Hub fromHub = hub(fromHubId, null, "출발 중앙 허브");
            Hub toHub = hub(toHubId, null, "도착 중앙 허브");

            HubRoutePathRequest request = new HubRoutePathRequest(fromHubId, toHubId);

            HubRoute hubRoute = hubRouteWithId(HubRoute.createHubRoute(fromHub, toHub, 60, 60));

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));

            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)).thenReturn(Optional.of(hubRoute));

            // when
            HubRoutePathResponse response = hubRouteService.getHubRoutePath(request);

            // then
            assertThat(response.segments()).hasSize(1);

            assertSegment(response.segments().get(0), 0, fromHubId, toHubId, 60, 60);

            assertThat(response.totalDistance()).isEqualTo(60);
            assertThat(response.totalDuration()).isEqualTo(60);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 출발 허브")
        void fromHubNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            HubRoutePathRequest request = new HubRoutePathRequest(fromHubId, toHubId);

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.getHubRoutePath(request))
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
            HubRoutePathRequest request = new HubRoutePathRequest(fromHubId, toHubId);

            Hub fromHub = hub(fromHubId, null, "출발 허브");

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.getHubRoutePath(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 출발 허브와 도착 허브가 동일함")
        void sameHub() {
            // given
            UUID hubId = UUID.randomUUID();
            HubRoutePathRequest request = new HubRoutePathRequest(hubId, hubId);

            Hub hub = hub(hubId, null, "허브");

            when(hubRepository.findByIdAndDeletedAtIsNull(hubId)).thenReturn(Optional.of(hub));

            // when & then
            assertThatThrownBy(() -> hubRouteService.getHubRoutePath(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SAME_HUB_ROUTE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("실패 - 등록되지 않은 구간 경로")
        void routeNotFound() {
            // given
            UUID fromHubId = UUID.randomUUID();
            UUID fromCentralId = UUID.randomUUID();
            UUID toHubId = UUID.randomUUID();
            UUID toCentralId = UUID.randomUUID();

            Hub fromHub = hub(fromHubId, fromCentralId, "출발 허브");
            Hub toHub = hub(toHubId, toCentralId, "도착 허브");

            HubRoutePathRequest request = new HubRoutePathRequest(fromHubId, toHubId);

            when(hubRepository.findByIdAndDeletedAtIsNull(fromHubId)).thenReturn(Optional.of(fromHub));
            when(hubRepository.findByIdAndDeletedAtIsNull(toHubId)).thenReturn(Optional.of(toHub));

            // 출발허브 -> 출발 중앙허브 구간이 등록되어 있지 않음
            when(hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, fromCentralId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubRouteService.getHubRoutePath(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.HUB_ROUTE_NOT_FOUND);
        }
    }

}

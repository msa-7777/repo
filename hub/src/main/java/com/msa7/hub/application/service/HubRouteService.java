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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubRouteService {
    private final HubRouteRepository hubRouteRepository;
    private final HubRouteSearchRepository hubRouteSearchRepository;
    private final HubRepository hubRepository;

    @Transactional
    public HubRouteResponse createHubRoute(HubRouteRequest request) {

        // 존재하는 허브인지 검증
        Hub fromHub = findExistingHub(request.fromHubId());
        Hub toHub = findExistingHub(request.toHubId());

        // 이미 등록된 허브 경로인지 검증
        ensureNotDuplicate(request.fromHubId(), request.toHubId());

        HubRoute hubRoute = HubRoute.createHubRoute(
                fromHub,
                toHub,
                request.duration(),
                request.distance()
        );

        try {
            HubRoute saved = hubRouteRepository.saveAndFlush(hubRoute);
            return HubRouteResponse.from(saved);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.HUB_ROUTE_ALREADY_EXISTS);
        }
    }

    @Transactional
    public HubRouteResponse updateHubRoute(UUID hubRouteId, HubRouteRequest request) {
        HubRoute hubRoute = findExistingHubRoute(hubRouteId);
        Hub fromHub = findExistingHub(request.fromHubId());
        Hub toHub = findExistingHub(request.toHubId());

        // 출발/도착 허브가 변경되는 경우에만 중복 경로 검증 (변경 없으면 자기 자신과 충돌하므로 제외)
        if (!hubRoute.getFromHubId().equals(request.fromHubId()) || !hubRoute.getToHubId().equals(request.toHubId())) {
            ensureNotDuplicate(request.fromHubId(), request.toHubId());
        }

        hubRoute.updateHubRoute(
                fromHub,
                toHub,
                request.duration(),
                request.distance()
        );

        try {
            HubRoute saved = hubRouteRepository.saveAndFlush(hubRoute);
            return HubRouteResponse.from(saved);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.HUB_ROUTE_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void deleteHubRoute(UUID hubRouteId, UUID deletedBy) {
        HubRoute hubRoute = findExistingHubRoute(hubRouteId);
        hubRoute.softDelete(deletedBy);
    }

    @Transactional(readOnly = true)
    public HubRouteResponse getHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = findExistingHubRoute(hubRouteId);
        return HubRouteResponse.from(hubRoute);
    }

    @Transactional(readOnly = true)
    public Page<HubRouteResponse> getHubRouteList(HubRouteSearchRequest request, Pageable pageable) {
        return hubRouteSearchRepository.search(request.fromHubId(), request.toHubId(), pageable)
                .map(HubRouteResponse::from);
    }

    @Transactional(readOnly = true)
    public HubRoutePathResponse getHubRoutePath(HubRoutePathRequest request) {

        // 존재 하는 허브인지 검증
        Hub fromHub = findExistingHub(request.fromHubId());
        Hub toHub = findExistingHub(request.toHubId());

        // 출발 허브 도착허브 같으면 에러 반환
        if (fromHub.getId().equals(toHub.getId())) {
            throw new BusinessException(ErrorCode.SAME_HUB_ROUTE_NOT_ALLOWED);
        }

        UUID fromCentralId = fromHub.isCentral() ? fromHub.getId() : fromHub.getCentralHubId();
        UUID toCentralId = toHub.isCentral() ? toHub.getId() : toHub.getCentralHubId();

        List<HubRouteSegment> segments = new ArrayList<>(); // 각 경로를 저장할 변수

        int totalDistance = 0;
        int totalDuration = 0;

        // 출발허브가 중앙 허브가 아니면 소속 중앙 허브로 보냄
        if (!fromHub.isCentral()) {
            addSegment(segments, fromHub.getId(), fromCentralId);
        }

        // 출발지, 목적지 같은 중앙 허브가 아니면 목적지 중앙 허브로 보냄
        if (!fromCentralId.equals(toCentralId)) {
            addSegment(segments,fromCentralId, toCentralId);
        }

        // 도착지가 중앙허브가 아니면 소속 일반 허브로 보냄
        if (!toHub.isCentral()) {
            addSegment(segments,toCentralId, toHub.getId());
        }

        // 총 거리, 시간 계산
        for (HubRouteSegment segment : segments) {
            totalDistance += segment.distance();
            totalDuration += segment.duration();
        }

        return new HubRoutePathResponse(totalDistance, totalDuration, segments);
    }

    private void addSegment(List<HubRouteSegment> segments, UUID fromId, UUID toId) {
        HubRoute hubRoute = findExistingHubRoute(fromId, toId);
        segments.add(new HubRouteSegment(
                segments.size(), fromId, toId, hubRoute.getDistance(), hubRoute.getDuration())
        );
    }

    private HubRoute findExistingHubRoute(UUID fromHubId, UUID toHubId) {
        return hubRouteRepository.findByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)
                .orElseThrow(() -> {
                    log.warn("허브 경로를 찾을 수 없음 - from: {}, to: {}", fromHubId, toHubId);
                    return new BusinessException(ErrorCode.HUB_ROUTE_NOT_FOUND);
                });
    }

    private HubRoute findExistingHubRoute(UUID huRouteId) {
         return hubRouteRepository.findByIdAndDeletedAtIsNull(huRouteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_ROUTE_NOT_FOUND));
    }

    private Hub findExistingHub(UUID hubId) {
        return hubRepository.findByIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
    }

    private void ensureNotDuplicate(UUID fromHubId, UUID toHubId) {
        if (hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(fromHubId, toHubId)) {
            throw new BusinessException(ErrorCode.HUB_ROUTE_ALREADY_EXISTS);
        }
    }
}

package com.msa7.hub.application.service;

import com.msa7.hub.domain.exception.BusinessException;
import com.msa7.hub.domain.exception.ErrorCode;
import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.domain.repository.HubRepository;
import com.msa7.hub.domain.repository.HubRouteRepository;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.response.HubRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HubRouteService {
    private final HubRouteRepository hubRouteRepository;
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
        HubRoute hubRoute = hubRouteRepository.findByIdAndDeletedAtIsNull(hubRouteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_ROUTE_NOT_FOUND));

        Hub fromHub = findExistingHub(request.fromHubId());
        Hub toHub = findExistingHub(request.toHubId());
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

    private HubRoute findExistingHubRoute(UUID huRouteId) {
         return hubRouteRepository.findByIdAndDeletedAtIsNull(huRouteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_ROUTE_NOT_FOUND));
    }

    private Hub findExistingHub(UUID hubId) {
        return hubRepository.findByIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
    }

    private void ensureNotDuplicate(UUID fromHubId, UUID toHubId) {
        if (hubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId)) {
            throw new BusinessException(ErrorCode.HUB_ROUTE_ALREADY_EXISTS);
        }
    }
}

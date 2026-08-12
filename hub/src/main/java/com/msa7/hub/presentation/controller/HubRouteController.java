package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubRouteService;
import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.presentation.api.HubRouteApi;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.request.HubRouteSearchRequest;
import com.msa7.hub.presentation.response.HubRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HubRouteController implements HubRouteApi {

    private final HubRouteService hubRouteService;

    @Override
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<RestApiResponse<HubRouteResponse>> createHubRoute(HubRouteRequest request) {
        HubRoute hubRoute = hubRouteService.createHubRoute(request.fromHubId(), request.toHubId(), request.duration(), request.distance());
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.ok(HubRouteResponse.from(hubRoute)));
    }

    @Override
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<RestApiResponse<HubRouteResponse>> updateHubRoute(UUID hubRouteId, HubRouteRequest request) {
        HubRoute hubRoute = hubRouteService.updateHubRoute(hubRouteId, request.fromHubId(), request.toHubId(), request.duration(), request.distance());
        return ResponseEntity.ok(RestApiResponse.ok(HubRouteResponse.from(hubRoute)));
    }

    @Override
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<RestApiResponse<Void>> deleteHubRoute(UUID hubRouteId, UUID userId) {
        hubRouteService.deleteHubRoute(hubRouteId, userId);
        return ResponseEntity.ok(RestApiResponse.ok(null));
    }

    @Override
    public ResponseEntity<RestApiResponse<HubRouteResponse>> getHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = hubRouteService.getHubRoute(hubRouteId);
        return ResponseEntity.ok(RestApiResponse.ok(HubRouteResponse.from(hubRoute)));
    }

    @Override
    public ResponseEntity<RestApiResponse<Page<HubRouteResponse>>> getHubRouteList(HubRouteSearchRequest request, Pageable pageable) {
        Page<HubRouteResponse> response = hubRouteService.getHubRouteList(request.fromHubId(), request.toHubId(), pageable)
                .map(HubRouteResponse::from);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }

}

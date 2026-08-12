package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubDeleteFacade;
import com.msa7.hub.application.service.HubService;
import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.presentation.api.HubApi;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.request.HubSearchRequest;
import com.msa7.hub.presentation.response.HubResponse;
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
public class HubController implements HubApi {

    private final HubService hubService;
    private final HubDeleteFacade hubDeleteFacade;

    @Override
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<RestApiResponse<HubResponse>> createHub(HubRequest request) {

        Hub hub = hubService.createHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.ok(HubResponse.from(hub)));
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<RestApiResponse<HubResponse>> updateHub(HubRequest request, UUID hubId) {

        Hub hub = hubService.updateHub(hubId, request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());
        return ResponseEntity.ok(RestApiResponse.ok(HubResponse.from(hub)));
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<RestApiResponse<Void>> deleteHub(UUID hubId, UUID userId) {

        hubDeleteFacade.deleteHub(hubId, userId);
        return ResponseEntity.ok(RestApiResponse.ok(null));
    }

    @Override
    public ResponseEntity<RestApiResponse<HubResponse>> getHub(UUID hubId) {
        Hub hub = hubService.getHub(hubId);
        return ResponseEntity.ok(RestApiResponse.ok(HubResponse.from(hub)));
    }

    @Override
    public ResponseEntity<RestApiResponse<Page<HubResponse>>> getHubList(HubSearchRequest request, Pageable pageable) {
        Page<HubResponse> response = hubService.getHubList(request.name(), request.address(), request.isCentral(), pageable).map(HubResponse::from);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }
}

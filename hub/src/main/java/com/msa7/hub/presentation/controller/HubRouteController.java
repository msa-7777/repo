package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubRouteService;
import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.presentation.request.HubRoutePathRequest;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.request.HubRouteSearchRequest;
import com.msa7.hub.presentation.response.HubRoutePathResponse;
import com.msa7.hub.presentation.response.HubRouteResponse;
import com.msa7.hub.presentation.response.RestApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HubRouteController {

    private final HubRouteService hubRouteService;

    @PostMapping("/hub-routes")
    public ResponseEntity<RestApiResponse<HubRouteResponse>> createHubRoute(
            @RequestBody @Valid HubRouteRequest request
    ) {
        HubRouteResponse response = hubRouteService.createHubRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.ok(response));
    }

    @PutMapping("/hub-routes/{hubRouteId}")
    public ResponseEntity<RestApiResponse<HubRouteResponse>> updateHubRoute(
            @PathVariable UUID hubRouteId,
            @RequestBody @Valid HubRouteRequest request
    ) {
        HubRouteResponse response = hubRouteService.updateHubRoute(hubRouteId, request);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }

    @DeleteMapping("/hub-routes/{hubRouteId}")
    public ResponseEntity<RestApiResponse<Void>> deleteHubRoute(
            @PathVariable UUID hubRouteId
    ) {
        hubRouteService.deleteHubRoute(hubRouteId, null); // TODO: 인증 구현 후 수정
        return ResponseEntity.ok(RestApiResponse.ok(null));
    }

    @GetMapping("/hub-routes/{hubRouteId}")
    public ResponseEntity<RestApiResponse<HubRouteResponse>> getHubRoute(
            @PathVariable UUID hubRouteId
    ) {
        HubRouteResponse response = hubRouteService.getHubRoute(hubRouteId);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }

    @GetMapping("/hub-routes")
    public ResponseEntity<RestApiResponse<Page<HubRouteResponse>>> getHubRouteList(
            @ModelAttribute HubRouteSearchRequest request,
            Pageable pageable
    ) {
        return null;
    }

    @GetMapping("/hub-routes/route")
    public ResponseEntity<RestApiResponse<HubRoutePathResponse>> getHubRoutePath(
            @Valid HubRoutePathRequest request
    ) {
        return null;
    }

}

package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubRouteService;
import com.msa7.hub.domain.model.HubRoute;
import com.msa7.hub.application.dto.HubRoutePathDto;
import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.presentation.request.HubRoutePathRequest;
import com.msa7.hub.presentation.request.HubRouteRequest;
import com.msa7.hub.presentation.request.HubRouteSearchRequest;
import com.msa7.hub.presentation.response.HubRoutePathResponse;
import com.msa7.hub.presentation.response.HubRouteResponse;
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
        HubRoute hubRoute = hubRouteService.createHubRoute(request.fromHubId(), request.toHubId(), request.duration(), request.distance());
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.ok(HubRouteResponse.from(hubRoute)));
    }

    @PutMapping("/hub-routes/{hubRouteId}")
    public ResponseEntity<RestApiResponse<HubRouteResponse>> updateHubRoute(
            @PathVariable UUID hubRouteId,
            @RequestBody @Valid HubRouteRequest request
    ) {
        HubRoute hubRoute = hubRouteService.updateHubRoute(hubRouteId, request.fromHubId(), request.toHubId(), request.duration(), request.distance());
        return ResponseEntity.ok(RestApiResponse.ok(HubRouteResponse.from(hubRoute)));
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
        HubRoute hubRoute = hubRouteService.getHubRoute(hubRouteId);
        return ResponseEntity.ok(RestApiResponse.ok(HubRouteResponse.from(hubRoute)));
    }

    @GetMapping("/hub-routes")
    public ResponseEntity<RestApiResponse<Page<HubRouteResponse>>> getHubRouteList(
            @ModelAttribute HubRouteSearchRequest request,
            Pageable pageable
    ) {
        Page<HubRouteResponse> response = hubRouteService.getHubRouteList(request.fromHubId(), request.toHubId(), pageable)
                .map(HubRouteResponse::from);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }

    @GetMapping("/hub-routes/path")
    public ResponseEntity<RestApiResponse<HubRoutePathResponse>> getHubRoutePath(
            @ModelAttribute @Valid HubRoutePathRequest request
    ) {
        HubRoutePathDto path = hubRouteService.getHubRoutePath(request.fromHubId(), request.toHubId());
        return ResponseEntity.ok(RestApiResponse.ok(HubRoutePathResponse.from(path)));
    }

}

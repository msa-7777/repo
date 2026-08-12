package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubRouteService;
import com.msa7.hub.application.service.HubService;
import com.msa7.hub.presentation.api.InternalHubApi;
import com.msa7.hub.presentation.response.HubRoutePathResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 서비스 간 내부 호출 전용 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/hubs")
public class InternalController implements InternalHubApi {

    private final HubService hubService;
    private final HubRouteService hubRouteService;

    @Override
    @GetMapping("/{hubId}/exists")
    public boolean checkHubExists(@PathVariable UUID hubId) {
        return hubService.existsHub(hubId);
    }

    @Override
    @GetMapping("/{startHubId}/routes/{endHubId}")
    public HubRoutePathResponse getRouteInfo(@PathVariable UUID startHubId, @PathVariable UUID endHubId) {
        return HubRoutePathResponse.from(hubRouteService.getHubRoutePath(startHubId, endHubId));
    }
}
package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubDeleteFacade;
import com.msa7.hub.application.service.HubService;
import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.request.HubSearchRequest;
import com.msa7.hub.presentation.response.HubResponse;
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
public class HubController {

    private final HubService hubService;
    private final HubDeleteFacade hubDeleteFacade;

    @PostMapping("/hubs")
    public ResponseEntity<RestApiResponse<HubResponse>> createHub(
            @RequestBody @Valid HubRequest request
    ) {

        Hub hub = hubService.createHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.ok(HubResponse.from(hub)));
    }

    @PutMapping("/hubs/{hubId}")
    public ResponseEntity<RestApiResponse<HubResponse>> updateHub(
            @RequestBody @Valid HubRequest request,
            @PathVariable UUID hubId
    ) {

        Hub hub = hubService.updateHub(hubId, request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());
        return ResponseEntity.ok(RestApiResponse.ok(HubResponse.from(hub)));
    }

    @DeleteMapping("/hubs/{hubId}")
    public ResponseEntity<RestApiResponse<Void>> deleteHub(
            @PathVariable UUID hubId
    ) {

        hubDeleteFacade.deleteHub(hubId, null); // TODO: 인증 구현 후 userId 값 수정
        return ResponseEntity.ok(RestApiResponse.ok(null));
    }

    @GetMapping("/hubs/{hubId}")
    public ResponseEntity<RestApiResponse<HubResponse>> getHub(
            @PathVariable UUID hubId
    ) {
        Hub hub = hubService.getHub(hubId);
        return ResponseEntity.ok(RestApiResponse.ok(HubResponse.from(hub)));
    }

    @GetMapping("/hubs")
    public ResponseEntity<RestApiResponse<Page<HubResponse>>> getHubList(
            @ModelAttribute HubSearchRequest request,
            Pageable pageable
    ) {
        Page<HubResponse> response = hubService.getHubList(request.name(), request.address(), request.isCentral(), pageable).map(HubResponse::from);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }
}

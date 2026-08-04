package com.msa7.hub.presentation.controller;

import com.msa7.hub.application.service.HubService;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.response.HubResponse;
import com.msa7.hub.presentation.response.RestApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HubController {

    private final HubService hubService;

    @PostMapping("/hubs")
    public ResponseEntity<RestApiResponse<HubResponse>> createHub(
            @RequestBody @Valid HubRequest request
    ) {

        HubResponse response = hubService.createHub(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.ok(response));
    }

    @PutMapping("/hubs/{hubId}")
    public ResponseEntity<RestApiResponse<HubResponse>> updateHub(
            @RequestBody @Valid HubRequest request,
            @PathVariable UUID hubId
    ) {

        HubResponse response = hubService.updateHub(hubId, request);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }

    @DeleteMapping("/hubs/{hubId}")
    public ResponseEntity<RestApiResponse<Void>> deleteHub(
            @PathVariable UUID hubId
    ) {

        hubService.deleteHub(hubId, null); // TODO: 인증 구현 후 userId 값 수정
        return ResponseEntity.ok(RestApiResponse.ok(null));
    }

    @GetMapping("/hubs/{hubId}")
    public ResponseEntity<RestApiResponse<HubResponse>> getHub(
            @PathVariable UUID hubId
    ) {
        HubResponse response = hubService.getHub(hubId);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }

    @GetMapping("/hubs")
    public ResponseEntity<RestApiResponse<Page<HubResponse>>> getHubList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Boolean isCentral,
            Pageable pageable
    ) {
        Page<HubResponse> response = hubService.getHubList(name, address, isCentral, pageable);
        return ResponseEntity.ok(RestApiResponse.ok(response));
    }
}

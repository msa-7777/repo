package com.msa7.hub.application.service;

import com.msa7.hub.domain.exception.BusinessException;
import com.msa7.hub.domain.exception.ErrorCode;
import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.repository.HubRepository;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.response.HubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HubService {
    private final HubRepository hubRepository;

    public HubResponse createHub(HubRequest request) {

        ensureCentralHubExists(request.centralHubId()); // 존재하는 중앙허브 인지 검증

        Hub hub = Hub.createHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());
        Hub saved = hubRepository.save(hub);

        return HubResponse.from(saved);
    }

    private void ensureCentralHubExists(UUID centralHubId) {
        if (centralHubId == null) {
            return;
        }
        hubRepository.findById(centralHubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CENTRAL_HUB_NOT_FOUND));
    }

}

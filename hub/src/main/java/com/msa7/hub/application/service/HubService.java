package com.msa7.hub.application.service;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.repository.HubRepository;
import com.msa7.hub.domain.repository.HubSearchRepository;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.presentation.request.HubRequest;
import com.msa7.hub.presentation.request.HubSearchRequest;
import com.msa7.hub.presentation.response.HubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HubService {
    private final HubRepository hubRepository;
    private final HubSearchRepository hubSearchRepository;

    @Transactional
    public HubResponse createHub(HubRequest request) {

        ensureCentralHubExists(request.centralHubId()); // 존재하는 중앙허브 인지 검증

        Hub hub = Hub.createHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());
        Hub saved = hubRepository.save(hub);

        return HubResponse.from(saved);
    }

    @Transactional
    public HubResponse updateHub(UUID hubId, HubRequest request) {
        Hub hub = findExistingHub(hubId);
        ensureCentralHubExists(request.centralHubId());
        hub.updateHub(request.centralHubId(), request.name(), request.latitude(), request.longitude(), request.address());

        Hub saved = hubRepository.saveAndFlush(hub); //updatedAt 필드 업데이트하기 위해
        return HubResponse.from(saved);
    }

    @Transactional
    public void deleteHub(UUID hubId, UUID userId) {
        // TODO: 삭제 후 delivery/company/inventory에 이벤트 발행 또는 동기 호출
        Hub hub = findExistingHub(hubId);
        hub.softDelete(userId);
    }

    @Transactional(readOnly = true)
    public HubResponse getHub(UUID hubId) {
        Hub hub = findExistingHub(hubId);
        return HubResponse.from(hub);
    }

    @Transactional(readOnly = true)
    public Page<HubResponse> getHubList(HubSearchRequest request, Pageable pageable) {
        return hubSearchRepository.search(request.name(), request.address(), request.isCentral(), pageable)
                .map(HubResponse::from);
    }

    private Hub findExistingHub(UUID hubId) {
        return hubRepository.findByIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
    }

    private void ensureCentralHubExists(UUID centralHubId) {
        if (centralHubId == null) {
            return;
        }
        hubRepository.findByIdAndDeletedAtIsNull(centralHubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CENTRAL_HUB_NOT_FOUND));
    }
}

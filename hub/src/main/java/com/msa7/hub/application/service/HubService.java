package com.msa7.hub.application.service;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.infrastructure.persistence.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HubService {
    private final HubRepository hubRepository;

    @Transactional
    public Hub createHub(UUID centralHubId, String name, BigDecimal latitude, BigDecimal longitude, String address) {

        ensureCentralHubExists(centralHubId); // 존재하는 중앙허브 인지 검증

        Hub hub = Hub.createHub(centralHubId, name, latitude, longitude, address);
        return hubRepository.save(hub);
    }

    @Transactional
    public Hub updateHub(UUID hubId, UUID centralHubId, String name, BigDecimal latitude, BigDecimal longitude, String address) {
        Hub hub = findExistingHub(hubId);
        ensureCentralHubExists(centralHubId);
        hub.updateHub(centralHubId, name, latitude, longitude, address);

        return hubRepository.save(hub);
    }

    @Transactional
    void softDelete(Hub hub, UUID deletedBy) {
        // 나를 중앙허브로 참조하고 있는 hub가 있는지 검증
        ensureNotReferencedByCentralHub(hub.getId());
        hub.softDelete(deletedBy);
        hubRepository.save(hub);
    }

    @Transactional(readOnly = true)
    public Hub getHub(UUID hubId) {
        return findExistingHub(hubId);
    }

    @Transactional(readOnly = true)
    public Page<Hub> getHubList(String name, String address, Boolean isCentral, Pageable pageable) {
        return hubRepository.search(name, address, isCentral, pageable);
    }

    @Transactional(readOnly = true)
    public boolean existsHub(UUID hubId) {
        return hubRepository.existsByIdAndDeletedAtIsNull(hubId);
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

    private void ensureNotReferencedByCentralHub(UUID hubId) {
        if (hubRepository.existsByCentralHubIdAndDeletedAtIsNull(hubId)) {
            throw new BusinessException(ErrorCode.HUB_REFERENCED_BY_CHILD_HUB);
        }
    }
}

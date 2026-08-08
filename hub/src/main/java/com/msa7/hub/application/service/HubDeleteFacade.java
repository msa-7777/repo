package com.msa7.hub.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.infrastructure.client.CompanyClient;
import com.msa7.hub.infrastructure.client.InventoryClient;
import com.msa7.hub.infrastructure.client.UserClient;
import com.msa7.hub.infrastructure.client.dto.CompanyResponse;
import com.msa7.hub.infrastructure.client.dto.InventoryResponse;
import com.msa7.hub.infrastructure.client.dto.PageResponse;
import com.msa7.hub.infrastructure.client.dto.UserResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubDeleteFacade {
    private final CompanyClient companyClient;
    private final InventoryClient inventoryClient;
    private final UserClient userClient;

    private final HubService hubService;

    public void deleteHub(UUID hubId, UUID deletedBy) {
        // 허브 조회 (짧은 readOnly 트랜잭션)
        Hub hub = hubService.getHub(hubId);

        // 참조 확인. 외부 api 호출 (트랜잭션 바깥)
        checkHubReference(hubId);

        // hub 삭제 (짧은 트랜잭션, soft delete)
        hubService.softDelete(hub, deletedBy);
    }

    private void checkHubReference(UUID hubId) {
        ensureNotReferencedByCompany(hubId);
        ensureNotReferencedByInventory(hubId);
        // TODO: user-service 모듈이 아직 없어서 호출 비활성화.
        // ensureNotReferencedByUser(hubId);
    }

    private void ensureNotReferencedByCompany(UUID hubId) {
        PageResponse<CompanyResponse> data = companyClient.getCompanyList(hubId).data();
        if (data.totalElements() > 0) {
            List<UUID> companyIds = data.content().stream().map(CompanyResponse::companyId).toList();
            log.warn("허브 삭제 차단 - hubId={}, 참조 companyIds={}", hubId, companyIds);
            throw new BusinessException(ErrorCode.HUB_REFERENCED_BY_COMPANY);
        }
    }

    private void ensureNotReferencedByInventory(UUID hubId) {
        PageResponse<InventoryResponse> data = inventoryClient.getInventoryList(hubId).data();
        if (data.totalElements() > 0) {
            List<UUID> inventoryIds = data.content().stream().map(InventoryResponse::inventoryId).toList();
            log.warn("허브 삭제 차단 - hubId={}, 참조 inventoryIds={}", hubId, inventoryIds);
            throw new BusinessException(ErrorCode.HUB_REFERENCED_BY_INVENTORY);
        }
    }

    private void ensureNotReferencedByUser(UUID hubId) {
        PageResponse<UserResponse> data = userClient.getUserList(hubId).data();
        if (data.totalElements() > 0) {
            List<UUID> userIds = data.content().stream().map(UserResponse::userId).toList();
            log.warn("허브 삭제 차단 - hubId={}, 참조 userIds={}", hubId, userIds);
            throw new BusinessException(ErrorCode.HUB_REFERENCED_BY_USER);
        }
    }
}

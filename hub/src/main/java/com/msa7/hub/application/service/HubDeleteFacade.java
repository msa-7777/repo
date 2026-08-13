package com.msa7.hub.application.service;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.infrastructure.client.CompanyClient;
import com.msa7.hub.infrastructure.client.DeliveryClient;
import com.msa7.hub.infrastructure.client.InventoryClient;
import com.msa7.hub.infrastructure.client.UserClient;
import com.msa7.hub.infrastructure.client.dto.CompanyResponse;
import com.msa7.hub.infrastructure.client.dto.InventoryResponse;
import com.msa7.hub.infrastructure.client.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubDeleteFacade {
    private final CompanyClient companyClient;
    private final InventoryClient inventoryClient;
    private final UserClient userClient;
    private final DeliveryClient deliveryClient;

    private final HubService hubService;
    private final HubRouteService hubRouteService;

    public void deleteHub(UUID hubId, UUID deletedBy) {
        // 허브 조회 + 상태 변경(Active -> DELETING)
        Hub hub = hubService.startDeleting(hubId);

        try {
            // HubRoute 참조 확인(짧은 readOnly 트랜잭션)
            hubRouteService.ensureNotReferencedByHub(hubId);

            // 참조 확인. 외부 api 호출 (트랜잭션 바깥)
            checkExternalHubReference(hubId);
        } catch (BusinessException e) {
            // 실패 시 상태 변경 (DELETING -> ACTIVE)
            hubService.cancelDeleting(hub);
            throw e;
        }


        // hub 삭제 (짧은 트랜잭션, soft delete, 상태 변경 (DELETING -> DELETED))
        hubService.softDelete(hub, deletedBy);
    }

    private void checkExternalHubReference(UUID hubId) {
        ensureNotReferencedByCompany(hubId);
        ensureNotReferencedByInventory(hubId);
        ensureNotReferencedByUser(hubId);
        ensureNotReferenceByDeliveryRoute(hubId);
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
        boolean isExist =  userClient.existsUserByHubId(hubId);
        if (isExist) {
            log.warn("허브 삭제 차단 - hubId={}", hubId);
            throw new BusinessException(ErrorCode.HUB_REFERENCED_BY_USER);
        }
    }

    private void ensureNotReferenceByDeliveryRoute(UUID hubId) {
        boolean isExist = deliveryClient.existsActiveDeliveryByHubId(hubId);
        if (isExist) {
            log.warn("허브 삭제 차단 - hubId={}", hubId);
            throw new BusinessException(ErrorCode.HUB_REFERENCED_BY_USER);
        }
    }
}

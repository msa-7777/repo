package com.msa7.hub.domain.model;

import com.msa7.hub.global.audit.BaseEntity;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_hubs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "central_hub_id")
    private UUID centralHubId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, length = 100)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HubState hubState;

    @Version
    private Long version;

    private Hub(UUID centralHubId, String name, BigDecimal latitude, BigDecimal longitude, String address, HubState hubState) {
        this.centralHubId = centralHubId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.hubState = hubState;
    }

    public boolean isCentral() {
        return centralHubId == null;
    }

    public static Hub createHub(UUID centralHubId, String name,  BigDecimal latitude, BigDecimal longitude, String address) {
        return new Hub(centralHubId, name, latitude, longitude, address, HubState.ACTIVE);
    }

    public void updateHub(UUID centralHubId,String name, BigDecimal latitude,  BigDecimal longitude, String address) {
        this.centralHubId = centralHubId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public void startDeleting() {
        if (this.hubState != HubState.ACTIVE) {
            throw new BusinessException(ErrorCode.HUB_ALREADY_DELETING);
        }
        this.hubState = HubState.DELETING;
    }

    public void softDelete(UUID deletedBy) {
        this.hubState = HubState.DELETED;
        super.softDelete(deletedBy);
    }

    public void cancelDeleting() {
        if (this.hubState != HubState.DELETING) {
            throw new IllegalStateException(
                    "DELETING 상태가 아닌 허브는 삭제를 취소할 수 없습니다. hubId=" + this.id + ", hubState=" + this.hubState
            );
        }
        this.hubState = HubState.ACTIVE;
    }
}

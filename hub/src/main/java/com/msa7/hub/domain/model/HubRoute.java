package com.msa7.hub.domain.model;

import com.msa7.hub.global.audit.BaseEntity;
import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "p_hub_routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_hub_route_from_to",
                        columnNames = {"from_hub_id", "to_hub_id", "unique_column"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HubRoute extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_route_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "from_hub_id", nullable = false)
    private UUID fromHubId;

    @Column(name = "to_hub_id", nullable = false)
    private UUID toHubId;

    @Column(nullable = false)
    private int duration; // 두 허브간 소요시간 (분)

    @Column(nullable = false)
    private int distance; // 두 허브간 거리 (km)

    // soft delete된 row끼리는 유니크 제약에서 서로 겹치지 않도록 하기 위한 컬럼
    // 활성 상태: 고정값 공유, 삭제 상태: 자기 자신의 id로 교체
    // 추후 ddl 작성 하게 되면 PostgreSQL partial unique index로 교체
    @Column(name = "unique_column", nullable = false)
    private UUID uniqueColumn;


    private HubRoute(UUID fromHubId, UUID toHubId, int duration, int distance) {
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.duration = duration;
        this.distance = distance;
        this.uniqueColumn = new UUID(0L, 0L);
    }

    public void softDelete(UUID deletedBy) {
        super.softDelete(deletedBy);
        this.uniqueColumn = this.id;
    }

    public static HubRoute createHubRoute(Hub fromHub, Hub toHub, int duration, int distance) {
        if (fromHub.getId().equals(toHub.getId())) {
            throw new BusinessException(ErrorCode.SAME_HUB_ROUTE_NOT_ALLOWED);
        }

        ensureValidRoute(fromHub, toHub);

        return new HubRoute(
                fromHub.getId(),
                toHub.getId(),
                duration,
                distance
        );
    }

    public void updateHubRoute(Hub fromHub, Hub toHub, int duration, int distance) {
        if (fromHub.getId().equals(toHub.getId())) {
            throw new BusinessException(ErrorCode.SAME_HUB_ROUTE_NOT_ALLOWED);
        }

        ensureValidRoute(fromHub, toHub);

        this.fromHubId = fromHub.getId();
        this.toHubId = toHub.getId();
        this.duration = duration;
        this.distance = distance;
    }

    // 중앙허브-중앙허브, 또는 중앙허브-자기 소속 스포크 조합만 허용
    private static void ensureValidRoute(Hub fromHub, Hub toHub) {
        boolean fromIsCentral = fromHub.isCentral();
        boolean toIsCentral = toHub.isCentral();

        // 중앙허브-중앙허브
        if (fromIsCentral && toIsCentral) {
            return;
        }

        // 중앙허브-자기 소속 스포크
        if (fromIsCentral && toHub.getCentralHubId().equals(fromHub.getId())) {
            return;
        }
        if (toIsCentral && fromHub.getCentralHubId().equals(toHub.getId())) {
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_HUB_ROUTE);
    }
}

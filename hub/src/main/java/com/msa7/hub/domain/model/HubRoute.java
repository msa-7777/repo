package com.msa7.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_hub_routes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HubRoute extends BaseEntity{
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

}

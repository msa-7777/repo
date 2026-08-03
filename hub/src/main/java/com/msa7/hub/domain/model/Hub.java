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


    private Hub(UUID centralHubId, String name, BigDecimal latitude, BigDecimal longitude, String address) {
        this.centralHubId = centralHubId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public static Hub createHub(UUID centralHubId, String name,  BigDecimal latitude, BigDecimal longitude, String address) {
        return new Hub(centralHubId, name, latitude, longitude, address);
    }

    public void updateHub(UUID centralHubId,String name, BigDecimal latitude,  BigDecimal longitude, String address) {
        this.centralHubId = centralHubId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}

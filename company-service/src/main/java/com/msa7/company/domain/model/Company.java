package com.msa7.company.domain.model;

import com.msa7.company.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompanyType type;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(nullable = false)
    private String address;

    private Company(String name, CompanyType type, UUID hubId, String address) {
        this.name = name;
        this.type = type;
        this.hubId = hubId;
        this.address = address;
    }

    public static Company create(String name, CompanyType type, UUID hubId, String address) {
        return new Company(name, type, hubId, address);
    }

    // 수정 로직 (비즈니스 메서드)
    public void update(String name, CompanyType type, UUID hubId, String address) {
        if (name != null) this.name = name;
        if (type != null) this.type = type;
        if (hubId != null) this.hubId = hubId;
        if (address != null) this.address = address;
    }

    // 삭제 로직 (Soft Delete)
    public void delete(UUID deletedBy) {
        super.softDelete(deletedBy);
    }
}
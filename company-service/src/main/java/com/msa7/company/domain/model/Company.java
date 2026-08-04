package com.msa7.company.domain.model;

import com.msa7.company.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType type;

    @Column(nullable = false)
    private UUID hubId;

    @Column(nullable = false, length = 255)
    private String address;

    public static Company create(String name, CompanyType type, UUID hubId, String address) {
        Company company = new Company();
        company.name = name;
        company.type = type;
        company.hubId = hubId;
        company.address = address;
        return company;
    }

    public void updateInfo(String name, CompanyType type, UUID hubId, String address) {
        this.name = name;
        this.type = type;
        this.hubId = hubId;
        this.address = address;
    }

}
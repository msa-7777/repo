package com.sparta.productservice.domain.product;

import com.sparta.productservice.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_product")
public class Product extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 100)
    private String name;

    private Product(UUID id, UUID companyId, String name) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
    }

    public static Product create(UUID companyId, String name) {
        return new Product(
                UUID.randomUUID(),
                companyId,
                name
        );
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void delete(UUID deletedBy) {
        super.delete(deletedBy);
    }
}
package com.msa7.v1.delivery.infra.persist;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@CreatedDate
	@Column(updatable = false, nullable = false)
	private LocalDateTime createdAt;
	@CreatedBy
	@Column(updatable = false)
	private UUID createdBy;
	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;
	@LastModifiedBy
	private UUID updatedBy;

	private LocalDateTime deletedAt;

	private UUID deletedBy;

	protected void markAsDeleted(UUID deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
	}

}

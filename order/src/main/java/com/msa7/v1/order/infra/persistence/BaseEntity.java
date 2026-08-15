package com.msa7.v1.order.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

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

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by")
	private UUID deletedBy;

	protected void setDeletedInfo(LocalDateTime deletedAt, UUID deletedBy) {
		this.deletedAt = deletedAt;
		this.deletedBy = deletedBy;
	}

}

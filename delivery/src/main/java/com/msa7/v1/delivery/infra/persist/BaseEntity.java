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
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(updatable = false)
	private UUID createdBy;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@LastModifiedBy
	private UUID updatedBy;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by")
	private UUID deletedBy;

	protected void setDeletedInfo(LocalDateTime deletedAt, UUID deletedBy) { // 파라미터 타입 변경
		this.deletedAt = deletedAt;
		this.deletedBy = deletedBy;
	}
}

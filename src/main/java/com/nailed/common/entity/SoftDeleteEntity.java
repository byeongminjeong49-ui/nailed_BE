package com.nailed.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Soft Delete가 필요한 Entity의 베이스 클래스
 * 대상: Product (status = DELETED), Member (status = WITHDRAWN)
 *
 * 사용법: public class Product extends SoftDeleteEntity { ... }
 *
 * 삭제 처리:
 *   product.softDelete();
 *
 * Repository 조회 시 주의:
 *   WHERE deleted_at IS NULL 조건 항상 포함
 *   또는 클래스에 @SQLRestriction("deleted_at IS NULL") 선언 (Hibernate 6+)
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class SoftDeleteEntity {

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

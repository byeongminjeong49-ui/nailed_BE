package com.nailed.domain.report.entity;

import com.nailed.domain.member.entity.Member;
import com.nailed.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Report {

    @Id
    @Column(name = "report_id", length = 20)
    private String reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Column(name = "target_type", length = 20, nullable = false)
    private String targetType;

    @Column(name = "reason_code", length = 30, nullable = false)
    private String reasonCode;

    @Column(name = "detail", length = 500)
    private String detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id")
    private Member targetMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id")
    private Product targetProduct;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "processed_reason", length = 500)
    private String processedReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void approve(String reason) {
        this.status = "APPROVED";
        this.processedReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.status = "REJECTED";
        this.processedReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    public void done(String reason) {
        this.status = "DONE";
        this.processedReason = reason;
        this.processedAt = LocalDateTime.now();
    }
}

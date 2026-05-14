package com.nailed.web.settlement.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long    orderId;
    @Column(nullable = false) private Long    productId;
    @Column(nullable = false) private Long    sellerId;
    @Column(nullable = false) private Integer orderAmount;
    @Column(nullable = false) private Integer commission;        // 수수료 2%
    @Column(nullable = false) private Integer settlementAmount;  // 거래금액 - 수수료
    @Column(nullable = false) private String  status;            // PENDING / COMPLETED / CANCELLED

    private String        productName;           // 정산 시점 상품명 스냅샷 (IA nld-906 정산 카드)
    private String        sellerAccountSnapshot; // 정산 시점 판매자 계좌 스냅샷
    private LocalDateTime completedAt;

    /**
     * 수수료·정산금액 자동 계산, status=PENDING 고정 (IA nld-605: commission=2%)
     * productName·sellerAccountSnapshot 은 정산 시점 스냅샷으로 영구 보존.
     */
    @Builder
    public Settlement(Long orderId, Long productId, Long sellerId,
                      Integer orderAmount, String productName, String sellerAccountSnapshot) {
        this.orderId               = orderId;
        this.productId             = productId;
        this.sellerId              = sellerId;
        this.orderAmount           = orderAmount;
        this.commission            = (int) Math.round(orderAmount * 0.02);
        this.settlementAmount      = orderAmount - this.commission;
        this.productName           = productName;
        this.sellerAccountSnapshot = sellerAccountSnapshot;
        this.status                = "PENDING";
    }

    /** PENDING → COMPLETED (IA nld-1110) */
    public void complete() {
        this.status      = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    /** PENDING → CANCELLED */
    public void cancel() {
        this.status = "CANCELLED";
    }
}

package com.nailed.web.product.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlists",
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"}))
@Getter
@NoArgsConstructor
public class Wishlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    public Wishlist(Long memberId, Long productId) {
        this.memberId = memberId;
        this.productId = productId;
    }
}

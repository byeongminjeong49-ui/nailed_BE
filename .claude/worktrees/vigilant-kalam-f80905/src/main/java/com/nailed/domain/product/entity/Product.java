package com.nailed.domain.product.entity;

import com.nailed.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductGroup category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private ProductGroup brand;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "price", nullable = false)
    private int price;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "condition_code", length = 20, nullable = false)
    private String conditionCode;

    @Column(name = "shipping_method", length = 20, nullable = false)
    private String shippingMethod;

    @Column(name = "size", length = 30)
    private String size;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ON_SALE";

    @Column(name = "hashtags", length = 500)
    private String hashtags;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "wishlist_count", nullable = false)
    @Builder.Default
    private int wishlistCount = 0;

    @Column(name = "deleted_reason", length = 500)
    private String deletedReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseWishlistCount() {
        this.wishlistCount++;
    }

    public void decreaseWishlistCount() {
        if (this.wishlistCount > 0) this.wishlistCount--;
    }

    public void reserve() {
        this.status = "RESERVED";
        this.updatedAt = LocalDateTime.now();
    }

    public void completeSale() {
        this.status = "SOLD";
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(String reason) {
        this.status = "DELETED";
        this.deletedReason = reason;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, int price, String description,
                       String conditionCode, String shippingMethod,
                       String size, String hashtags) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.conditionCode = conditionCode;
        this.shippingMethod = shippingMethod;
        this.size = size;
        this.hashtags = hashtags;
        this.updatedAt = LocalDateTime.now();
    }
}

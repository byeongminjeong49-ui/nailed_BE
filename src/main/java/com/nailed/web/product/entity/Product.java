package com.nailed.web.product.entity;

import com.nailed.common.entity.SoftDeleteEntity;
import com.nailed.common.enums.CategoryCode;
import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private Long sellerId;
    private String title;
    private int price;
    private String description;
    @Enumerated(EnumType.STRING)
    private ProductCondition conditionCode;
    @Enumerated(EnumType.STRING)
    private CategoryCode categoryCode;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int wishlistCount = 0;

    public Product(Long sellerId, String title, int price, String description,
                   ProductCondition conditionCode, CategoryCode categoryCode, String imageUrl) {
        this.sellerId = sellerId;
        this.title = title;
        this.price = price;
        this.description = description;
        this.conditionCode = conditionCode;
        this.categoryCode = categoryCode;
        this.imageUrl = imageUrl;
        this.status = ProductStatus.ON_SALE;
    }

    public void update(String title, int price, String description,
                       ProductCondition conditionCode, CategoryCode categoryCode, String imageUrl) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.conditionCode = conditionCode;
        this.categoryCode = categoryCode;
        this.imageUrl = imageUrl;
    }

<<<<<<< HEAD
    public void markAsSoldOut() {
        this.status = ProductStatus.SOLD_OUT;
    }

    public void restoreToOnSale() {
        if (this.status != ProductStatus.DELETED) {
            this.status = ProductStatus.ON_SALE;
        }
    }

    public void delete() {
=======
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementWishlistCount() {
        this.wishlistCount++;
    }

    public void decrementWishlistCount() {
        if (this.wishlistCount > 0) this.wishlistCount--;
    }

    public void changeStatus(ProductStatus newStatus) {
        this.status = newStatus;
    }

    public void validateNotDeleted() {
>>>>>>> main
        if (this.status == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.PRODUCT_DELETED);
        }
    }

    public void delete() {
        validateNotDeleted();
        this.status = ProductStatus.DELETED;
        super.softDelete();
    }
}

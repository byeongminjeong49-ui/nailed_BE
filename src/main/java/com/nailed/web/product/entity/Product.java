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

@Entity // 데이터베이스 테이블과 연결되는 엔티티라고 JPA에게 알려줌
@Table(name = "products") // products라는 이름의 테이블과 매핑된다고 지정
@Getter // 든 필드의 getter 메서드를 자동 생성 ex) getProductId(), getTitle()
@NoArgsConstructor // 매개변수 없는 기본 생성자를 자동 생성
public class Product extends SoftDeleteEntity {

    @Id // 테이블의 기본 키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) //데이터베이스가 자동으로 ID를 생성 (AUTO_INCREMENT). 상품 등록 시 ID는 자동으로 1씩 증가
    private Long productId;

    private String sellerId;
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

    public Product(String sellerId, String title, int price, String description,
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

    public void delete() {
        if (this.status == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.PRODUCT_DELETED);
        }
        this.status = ProductStatus.DELETED;
        super.softDelete();
    }
}

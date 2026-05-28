package com.nailed.web.product.entity;

import com.nailed.common.enums.GroupType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 카테고리 / 브랜드 통합 계층형 마스터 테이블
 * - group_type = CATEGORY: 상품 카테고리 (parent_id로 2단계 계층 구성)
 * - group_type = BRAND   : 상품 브랜드 (parent_id = null)
 * - DB 스키마에 created_at / updated_at 없음 → BaseEntity 미상속
 */
@Entity
@Table(name = "product_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    // CATEGORY / BRAND 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", length = 20, nullable = false)
    private GroupType groupType;

    // 상위 카테고리 (최상위 또는 브랜드면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductGroup parent;

    // 식별 코드 (예: CLOTHES_TOP, NIKE)
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    // 화면 표시명
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    // ── 비즈니스 메서드 ──────────────────────────────

    public boolean isCategory() {
        return this.groupType == GroupType.CATEGORY;
    }

    public boolean isBrand() {
        return this.groupType == GroupType.BRAND;
    }

    // BRAND 타입 또는 럭셔리 서브카테고리(CATEGORY 타입이지만 브랜드 역할)를 브랜드 참조로 허용
    public boolean isValidBrandRef() {
        if (this.groupType == GroupType.BRAND) return true;
        return this.groupType == GroupType.CATEGORY
                && this.code.startsWith("LUXURY_")
                && !this.code.equals("LUXURY")
                && !this.code.equals("LUXURY_BRAND");
    }

    public void updateName(String name) {
        this.name = name;
    }
}

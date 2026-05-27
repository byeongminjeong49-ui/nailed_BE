package com.nailed.web.product.repository;

import com.nailed.common.enums.GroupType;
import com.nailed.web.product.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {

    // 타입별 전체 조회 (카테고리 목록, 브랜드 목록)
    List<ProductGroup> findByGroupTypeOrderByNameAsc(GroupType groupType);

    // 코드로 단건 조회 (관리자 중복 등록 방지)
    boolean existsByCode(String code);

    // 카테고리 목록 + parent 함께 조회 (LazyInitializationException 방지)
    @Query("SELECT g FROM ProductGroup g LEFT JOIN FETCH g.parent WHERE g.groupType = :groupType")
    List<ProductGroup> findByGroupTypeWithParent(@Param("groupType") GroupType groupType);

    // 브랜드 + 럭셔리 서브카테고리 함께 조회 (브랜드 드롭다운용)
    @Query("SELECT g FROM ProductGroup g LEFT JOIN FETCH g.parent WHERE g.groupType = 'BRAND' OR (g.groupType = 'CATEGORY' AND g.code LIKE 'LUXURY\\_%' ESCAPE '\\' AND g.code NOT IN ('LUXURY', 'LUXURY_BRAND')) ORDER BY g.name ASC")
    List<ProductGroup> findBrandsIncludingLuxury();
}

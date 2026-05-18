package com.nailed.web.product.repository;

import com.nailed.common.enums.GroupType;
import com.nailed.web.product.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {

    // 타입별 전체 조회 (카테고리 목록, 브랜드 목록)
    List<ProductGroup> findByGroupTypeOrderByNameAsc(GroupType groupType);

    // 코드로 단건 조회 (관리자 중복 등록 방지)
    boolean existsByCode(String code);
}

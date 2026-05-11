package com.nailed.domain.product.repository;

import com.nailed.domain.product.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {

    List<ProductGroup> findByGroupTypeAndParentIsNull(String groupType);

    List<ProductGroup> findByGroupTypeAndParentGroupId(String groupType, Long parentId);

    Optional<ProductGroup> findByCode(String code);

    boolean existsByCode(String code);
}

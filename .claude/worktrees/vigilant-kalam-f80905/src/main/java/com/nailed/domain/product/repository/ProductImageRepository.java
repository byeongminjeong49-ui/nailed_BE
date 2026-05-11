package com.nailed.domain.product.repository;

import com.nailed.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductProductIdOrderBySortOrderAsc(Long productId);

    void deleteByProductProductId(Long productId);
}

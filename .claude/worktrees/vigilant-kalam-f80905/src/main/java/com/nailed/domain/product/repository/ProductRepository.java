package com.nailed.domain.product.repository;

import com.nailed.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findBySellerMemberIdAndStatusNot(String sellerId, String status, Pageable pageable);

    Page<Product> findByCategoryCategoryCodeAndStatusNot(String categoryCode, String status, Pageable pageable);
}

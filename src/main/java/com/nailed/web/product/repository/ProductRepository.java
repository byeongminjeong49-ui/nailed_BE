package com.nailed.web.product.repository;

import com.nailed.common.enums.ProductStatus;
import com.nailed.web.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerIdAndStatusNot(String sellerId, ProductStatus status);
}

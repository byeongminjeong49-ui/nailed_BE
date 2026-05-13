package com.nailed.web.product.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController // REST API 컨트롤러임을 선언 (JSON 응답)
@RequestMapping("/api/products")
@RequiredArgsConstructor // lombok: 생성자 자동 생성 (ProductService 주입)
public class ProductController {

    private final ProductService productService; // Service 주입 (의존성)

    // POST /api/products
    @PostMapping
    public ApiResponse<Long> register(@RequestBody ProductRequest.Register request) {
        Long productId = productService.register(request);
        return ApiResponse.success(productId);
    }

    // GET /api/products/{productId}
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse.Detail> getDetail(@PathVariable Long productId) {
        return ApiResponse.success(productService.getDetail(productId));
    }

    // GET /api/products/seller/{sellerId}
    @GetMapping("/seller/{sellerId}")
    public ApiResponse<List<ProductResponse.Summary>> getListBySeller(@PathVariable String sellerId) {
        return ApiResponse.success(productService.getListBySeller(sellerId));
    }

    // PUT /api/products/{productId}
    @PutMapping("/{productId}")
    public ApiResponse<Void> update(@PathVariable Long productId,
                                    @RequestBody ProductRequest.Update request) {
        productService.update(productId, request);
        return ApiResponse.success();
    }

    // DELETE /api/products/{productId}
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ApiResponse.success();
    }
}
package com.nailed.domain.product.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.product.dto.ProductRequest;
import com.nailed.domain.product.dto.ProductResponse;
import com.nailed.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(
            @RequestParam String sellerId,
            @Valid @RequestBody ProductRequest.Register request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.register(sellerId, request)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse.Detail>> getDetail(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getDetail(productId)));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse.Summary>>> getSellerProducts(
            @PathVariable String sellerId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getSellerProducts(sellerId, pageable)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long productId,
            @RequestParam String sellerId,
            @Valid @RequestBody ProductRequest.Update request) {
        productService.update(productId, sellerId, request);
        return ResponseEntity.ok(ApiResponse.ok("상품이 수정되었습니다.", null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId,
            @RequestParam String sellerId,
            @RequestBody ProductRequest.Delete request) {
        productService.delete(productId, sellerId, request);
        return ResponseEntity.ok(ApiResponse.ok("상품이 삭제되었습니다.", null));
    }
}

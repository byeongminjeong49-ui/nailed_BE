package com.nailed.web.product.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<Long> register(@RequestBody ProductRequest.Register request) {
        return ApiResponse.success(productService.register(request));
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse.Card>> listByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.search(null, category, null, null, null, page, size));
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductResponse.Card>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String conditionCode,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ApiResponse.success(productService.search(keyword, categoryCode, conditionCode, minPrice, maxPrice, page, size));
    }

    @GetMapping("/new")
    public ApiResponse<List<ProductResponse.Card>> getNewProducts() {
        return ApiResponse.success(productService.getNewProducts());
    }

    @GetMapping("/popular")
    public ApiResponse<List<ProductResponse.Card>> getPopularProducts() {
        return ApiResponse.success(productService.getPopularProducts());
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse.Detail> getDetail(@PathVariable Long productId) {
        return ApiResponse.success(productService.getDetail(productId));
    }

    @GetMapping("/seller/{sellerId}")
    public ApiResponse<List<ProductResponse.Summary>> getListBySeller(@PathVariable Long sellerId) {
        return ApiResponse.success(productService.getListBySeller(sellerId));
    }

    @PutMapping("/{productId}")
    public ApiResponse<Void> update(@PathVariable Long productId, @RequestBody ProductRequest.Update request) {
        productService.update(productId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ApiResponse.success();
    }

    @PostMapping("/{productId}/view")
    public ApiResponse<Void> incrementView(@PathVariable Long productId) {
        productService.incrementViewCount(productId);
        return ApiResponse.success();
    }

    @GetMapping("/my")
    public ApiResponse<List<ProductResponse.Card>> getMyProducts(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(productService.getMyProducts(status));
    }

    @GetMapping("/wishlist")
    public ApiResponse<List<ProductResponse.Card>> getMyWishlist() {
        return ApiResponse.success(productService.getMyWishlist());
    }

    @PostMapping("/{productId}/wishlist")
    public ApiResponse<Void> addWishlist(@PathVariable Long productId) {
        productService.addWishlist(productId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{productId}/wishlist")
    public ApiResponse<Void> removeWishlist(@PathVariable Long productId) {
        productService.removeWishlist(productId);
        return ApiResponse.success();
    }

    @PatchMapping("/{productId}/status")
    public ApiResponse<Void> changeStatus(@PathVariable Long productId,
                                          @RequestBody ProductRequest.StatusUpdate request) {
        productService.changeStatus(productId, request.status());
        return ApiResponse.success();
    }
}

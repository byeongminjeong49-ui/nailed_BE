package com.nailed.web.product.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberProductController {

    private final ProductService productService;

    // GET /api/members/me/products?status=ON_SALE
    @GetMapping("/products")
    public ApiResponse<List<ProductResponse.Card>> getMyProducts(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(productService.getMyProducts(status));
    }

    // GET /api/members/me/wishlist
    @GetMapping("/wishlist")
    public ApiResponse<List<ProductResponse.Card>> getMyWishlist() {
        return ApiResponse.success(productService.getMyWishlist());
    }

    // DELETE /api/members/me/wishlist/{productId}
    @DeleteMapping("/wishlist/{productId}")
    public ApiResponse<Void> removeMyWishlist(@PathVariable Long productId) {
        productService.removeWishlist(productId);
        return ApiResponse.success();
    }
}

package com.nailed.domain.wishlist.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.wishlist.dto.WishlistResponse;
import com.nailed.domain.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(
            @RequestParam String memberId,
            @RequestParam Long productId) {
        wishlistService.add(memberId, productId);
        return ResponseEntity.ok(ApiResponse.ok("찜 목록에 추가되었습니다.", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> remove(
            @RequestParam String memberId,
            @RequestParam Long productId) {
        wishlistService.remove(memberId, productId);
        return ResponseEntity.ok(ApiResponse.ok("찜 목록에서 제거되었습니다.", null));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<WishlistResponse.Item>>> getMyWishlist(
            @PathVariable String memberId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getMyWishlist(memberId, pageable)));
    }
}

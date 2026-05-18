package com.nailed.web.product.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.common.response.PageResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.service.ProductService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── 이미지 업로드 (로그인 필요) ───────────────────────────

    @PostMapping("/image-upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String imageUrl = productService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    // ── 상품 등록 (로그인 필요) ───────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> register(
            @Valid @RequestBody ProductRequest.Create request) {
        String sellerId = SecurityUtil.getCurrentMemberId();
        Long productId = productService.register(sellerId, request);
        return ResponseEntity.ok(ApiResponse.success(productId));
    }

    // ── 카테고리별 상품 목록 (비로그인 가능) ──────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> getList(
            @RequestParam Long categoryId,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getList(categoryId, pageable)));
    }

    // ── 홈 추천: 최신 상품 6개 (비로그인 가능) ────────────────

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getNewProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getNewProducts()));
    }

    // ── 홈 인기 TOP 10 (비로그인 가능) ───────────────────────

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getPopularProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getPopularProducts()));
    }

    // ── 검색 + 필터 (비로그인 가능) ──────────────────────────

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String conditionCode,
            @RequestParam(required = false) String size,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.search(categoryId, keyword, minPrice, maxPrice, conditionCode, size, pageable)));
    }

    // ── 상품 상세 (비로그인 가능) ─────────────────────────────

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse.Detail>> getDetail(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getDetail(productId)));
    }

    // ── 조회수 +1 (비로그인 가능, 세션 쿠키로 중복 방지) ──────

    @PostMapping("/{productId}/view")
    public ResponseEntity<ApiResponse<Void>> increaseViewCount(
            @PathVariable Long productId,
            HttpServletRequest request,
            HttpServletResponse response) {

        String cookieName = "viewed_" + productId;

        // 이미 조회한 상품이면 카운트 증가 안 함
        boolean alreadyViewed = request.getCookies() != null
                && Arrays.stream(request.getCookies())
                         .anyMatch(c -> cookieName.equals(c.getName()));

        if (!alreadyViewed) {
            productService.increaseViewCount(productId);

            // 세션 쿠키 설정 (브라우저 닫으면 만료 → 재방문 시 재집계)
            Cookie cookie = new Cookie(cookieName, "1");
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

    // ── 상품 수정 (본인만 가능) ───────────────────────────────

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest.Update request) {
        String sellerId = SecurityUtil.getCurrentMemberId();
        productService.update(productId, sellerId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ── 상품 삭제 (본인만 가능, 소프트 삭제) ─────────────────

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId,
            @RequestParam(required = false) String reason) {
        String sellerId = SecurityUtil.getCurrentMemberId();
        productService.delete(productId, sellerId, reason);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ── 판매 상태 변경 (본인만 가능) ─────────────────────────

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest.StatusUpdate request) {
        String sellerId = SecurityUtil.getCurrentMemberId();
        productService.changeStatus(productId, sellerId, request.productStatus());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ── 내 판매 상품 목록 (로그인 필요) ──────────────────────

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> getMyProducts(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String sellerId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(ApiResponse.success(productService.getMyProducts(sellerId, status, pageable)));
    }
}

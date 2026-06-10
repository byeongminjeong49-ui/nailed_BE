package com.nailed.web.product.controller;

import com.nailed.common.enums.GroupType;
import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.SizeCode;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.ApiResponse;
import com.nailed.common.response.PageResponse;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.entity.ProductGroup;
import com.nailed.web.product.repository.ProductGroupRepository;
import com.nailed.web.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductGroupRepository productGroupRepository;

    // ── 카테고리 목록 (비로그인 가능) ────────────────────────

    public record CategoryDto(Long groupId, String code, String name, String parentCode, String sizeType) {}
    public record ConditionDto(String code, String label) {}
    public record SizeDto(String value, String sizeType) {}

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        List<ProductGroup> groups =
                productGroupRepository.findByGroupTypeWithParent(GroupType.CATEGORY);

        List<CategoryDto> result = new ArrayList<>();
        for (ProductGroup g : groups) {
            result.add(new CategoryDto(
                    g.getGroupId(),
                    g.getCode(),
                    g.getName(),
                    g.getParent() != null ? g.getParent().getCode() : null,
                    g.getSizeType()
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/conditions")
    public ResponseEntity<ApiResponse<List<ConditionDto>>> getConditions() {
        List<ConditionDto> list = new ArrayList<>();
        for (ProductCondition c : ProductCondition.values()) {
            list.add(new ConditionDto(c.name(), c.getLabel()));
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/sizes")
    public ResponseEntity<ApiResponse<List<SizeDto>>> getSizes() {
        List<SizeDto> list = new ArrayList<>();
        for (SizeCode s : SizeCode.values()) {
            list.add(new SizeDto(s.getValue(), s.getSizeType().name()));
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // ── 브랜드 목록 (비로그인 가능) ──────────────────────────

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getBrands() {
        List<ProductGroup> groups =
                productGroupRepository.findBrandsIncludingLuxury();

        List<CategoryDto> result = new ArrayList<>();
        for (ProductGroup g : groups) {
            result.add(new CategoryDto(
                    g.getGroupId(),
                    g.getCode(),
                    g.getName(),
                    g.getParent() != null ? g.getParent().getCode() : null,
                    g.getSizeType()
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

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
    // categoryCode: "MENS", "MENS_OUTER" 등 코드 prefix → 하위 카테고리 전체 조회
    // categoryId : 기존 groupId 기반 단일 카테고리 조회 (하위 호환)

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false, defaultValue = "false") boolean excludeSold,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String conditionCode,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (categoryCode != null && !categoryCode.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(productService.getListByCode(
                    categoryCode, minPrice, maxPrice, gender, excludeSold, productSize, conditionCode, sortBy, pageable)));
        }
        if (categoryId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return ResponseEntity.ok(ApiResponse.success(productService.getList(
                categoryId, minPrice, maxPrice, gender, excludeSold, productSize, conditionCode, sortBy, pageable)));
    }

    // ── 홈 추천: 최신 상품 6개 (비로그인 가능) ────────────────

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getNewProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getNewProducts()));
    }

    // ── 홈 인기 TOP 5 (비로그인 가능) ────────────────────────

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getPopularProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getPopularProducts()));
    }

    // ── 메인 카테고리 랜덤 상품 (비로그인 가능) ─────────────

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getRandomProducts(
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(productService.getRandomProducts(size)));
    }

    // ── 검색 + 필터 (비로그인 가능) ──────────────────────────

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse.Summary>>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String conditionCode,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false, defaultValue = "false") boolean excludeSold,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @PageableDefault(size = 15) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.search(categoryId, keyword, minPrice, maxPrice, conditionCode, productSize,
                        gender, excludeSold, sortBy, pageable)));
    }

    // ── 상품 클릭 → 상세 페이지 진입 시 호출 (비로그인 가능) ──

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse.Detail>> getDetail(
            @PathVariable Long productId) {
        String memberId = SecurityUtil.getCurrentMemberIdOrNull();
        return ResponseEntity.ok(ApiResponse.success(productService.getDetail(productId, memberId)));
    }

    // ── 조회수 +1 (비로그인 가능, 서버 세션으로 중복 방지) ──────

    @PostMapping("/{productId}/view")
    public ResponseEntity<ApiResponse<Void>> increaseViewCount(
            @PathVariable Long productId,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        String key = "viewed_" + productId;

        if (session.getAttribute(key) == null) {
            productService.increaseViewCount(productId);
            session.setAttribute(key, true);
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

    // ── 판매자의 다른 상품 (비로그인 가능) ────────────────────

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getSellerProducts(
            @PathVariable String sellerId,
            @RequestParam(required = false) Long exclude) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getSellerProducts(sellerId, exclude != null ? exclude : -1L)));
    }

    // ── 같은 카테고리 "비슷한 상품" (비로그인 가능) ──────────

    @GetMapping("/{productId}/related")
    public ResponseEntity<ApiResponse<List<ProductResponse.Summary>>> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getRelatedProducts(productId, size)));
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

}

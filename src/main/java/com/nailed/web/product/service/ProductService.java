package com.nailed.web.product.service;

import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.common.util.EnumUtil;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.product.dto.ProductRequest;
import com.nailed.web.product.dto.ProductResponse;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.entity.ProductGroup;
import com.nailed.web.product.entity.ProductImage;
import com.nailed.web.product.repository.ProductGroupRepository;
import com.nailed.web.product.repository.ProductImageRepository;
import com.nailed.web.product.repository.ProductRepository;
import com.nailed.web.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final ProductImageRepository productImageRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    // application.properties에 file.upload.path 미정의 시 기본 경로 사용
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    // ── 이미지 업로드 ─────────────────────────────────────────

    /**
     * 이미지 파일을 Tomcat 로컬 디렉토리에 저장하고 URL을 반환한다.
     * 상품 등록(register)과 별개로 호출되며, URL은 이후 register 요청에 포함된다.
     *
     * 주의: application.properties에 spring.servlet.multipart.max-file-size=5MB 설정 권장
     */
    public String uploadImage(MultipartFile file) {
        // 허용 확장자 검증 (jpg / png / webp)
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!List.of("jpg", "jpeg", "png", "webp").contains(ext)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 5MB 초과 검증
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new CustomException(ErrorCode.PRODUCT_IMAGE_LIMIT_EXCEEDED);
        }

        try {
            // UUID 기반 유니크 파일명 생성 후 저장
            String savedFileName = UUID.randomUUID() + "." + ext;
            Path targetPath = Paths.get(uploadPath, savedFileName);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // DB에 저장될 상대 경로 반환
            return "/uploads/" + savedFileName;

        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ── 상품 등록 ─────────────────────────────────────────────

    @Transactional
    public Long register(String sellerId, ProductRequest.Create req) {
        Member seller = findMember(sellerId);
        ProductGroup category = findCategory(req.categoryId());
        ProductGroup brand = req.brandId() != null ? findBrand(req.brandId()) : null;

        // 상태등급 코드 검증 (S/A/B/C/D)
        ProductCondition condition = EnumUtil.parse(ProductCondition.class, req.conditionCode(), ErrorCode.INVALID_INPUT_VALUE);

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .brand(brand)
                .title(req.title())
                .price(req.price())
                .description(req.description())
                .conditionCode(condition)
                .size(req.size())
                .hashtags(req.hashtags())
                .build();

        Product saved = productRepository.save(product);

        // 이미지 저장 (index = sort_order, 0번이 대표 이미지)
        saveImages(saved, req.imageUrls());

        return saved.getProductId();
    }

    // ── 상품 상세 조회 ────────────────────────────────────────

    public ProductResponse.Detail getDetail(Long productId) {
        Product product = findActiveProduct(productId);

        // 이미지 목록 (sort_order 순)
        List<String> imageUrls = productImageRepository
                .findByProductProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        // 판매자 프로필 카드 구성
        ProductResponse.SellerInfo sellerInfo = buildSellerInfo(product.getSeller());

        return ProductResponse.Detail.from(product, imageUrls, sellerInfo);
    }

    // ── 조회수 +1 ─────────────────────────────────────────────

    /**
     * DB에서 직접 +1하여 동시 요청 시 Lost Update를 방지한다.
     * 세션 쿠키 중복 체크는 Controller에서 처리한다.
     */
    @Transactional
    public void increaseViewCount(Long productId) {
        // incrementViewCount가 0을 반환하면 존재하지 않거나 삭제된 상품
        int updated = productRepository.incrementViewCount(productId, ProductStatus.DELETED);
        if (updated == 0) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    // ── 카테고리별 목록 ───────────────────────────────────────

    public PageResponse<ProductResponse.Summary> getList(Long categoryId, Pageable pageable) {
        Page<Product> page = productRepository
                .findByCategoryGroupIdAndProductStatusNot(categoryId, ProductStatus.DELETED, pageable);
        return toSummaryPage(page);
    }

    // ── 검색 + 다중 필터 ──────────────────────────────────────

    public PageResponse<ProductResponse.Summary> search(Long categoryId, String keyword,
                                                        Integer minPrice, Integer maxPrice,
                                                        String conditionCode, String size,
                                                        Pageable pageable) {
        // conditionCode가 전달된 경우에만 Enum 변환 (없으면 null → 필터 미적용)
        ProductCondition condition = (conditionCode != null && !conditionCode.isBlank())
                ? EnumUtil.parse(ProductCondition.class, conditionCode, ErrorCode.INVALID_INPUT_VALUE)
                : null;

        Page<Product> page = productRepository.search(
                ProductStatus.ON_SALE, categoryId, keyword,
                minPrice, maxPrice, condition, size, pageable);

        return toSummaryPage(page);
    }

    // ── 홈 추천: 최신 6개 ────────────────────────────────────

    public List<ProductResponse.Summary> getNewProducts() {
        List<Product> products = productRepository
                .findTop6ByProductStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE);
        return toSummaryList(products);
    }

    // ── 홈 인기 TOP 10 ────────────────────────────────────────

    public List<ProductResponse.Summary> getPopularProducts() {
        List<Product> products = productRepository.findPopularTop10(ProductStatus.ON_SALE.name());
        return toSummaryList(products);
    }

    // ── 상품 수정 ─────────────────────────────────────────────

    @Transactional
    public void update(Long productId, String sellerId, ProductRequest.Update req) {
        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

        // 거래 진행 중인 상품은 수정 불가
        if (product.getProductStatus() == ProductStatus.RESERVED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        ProductGroup category = findCategory(req.categoryId());
        ProductGroup brand = req.brandId() != null ? findBrand(req.brandId()) : null;
        ProductCondition condition = EnumUtil.parse(ProductCondition.class, req.conditionCode(), ErrorCode.INVALID_INPUT_VALUE);

        product.update(req.title(), category, brand, req.price(),
                req.description(), condition, req.size(), req.hashtags());

        // 이미지 전체 교체 (기존 삭제 후 재등록)
        product.clearImages();
        saveImages(product, req.imageUrls());
    }

    // ── 상품 삭제 (소프트) ────────────────────────────────────

    @Transactional
    public void delete(Long productId, String sellerId, String reason) {
        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

        // 거래 진행 중(RESERVED)이면 삭제 불가
        if (product.getProductStatus() == ProductStatus.RESERVED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        product.delete(reason);
    }

    // ── 판매 상태 변경 ────────────────────────────────────────

    @Transactional
    public void changeStatus(Long productId, String sellerId, String statusStr) {
        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

        ProductStatus newStatus = EnumUtil.parse(ProductStatus.class, statusStr, ErrorCode.INVALID_INPUT_VALUE);

        // DELETED는 delete API로만 처리
        if (newStatus == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 현재 상태 그대로면 변경 불필요
        if (product.getProductStatus() == newStatus) {
            return;
        }

        switch (newStatus) {
            case ON_SALE  -> product.restore();
            case RESERVED -> product.reserve();
            case SOLD     -> product.completeSale();
            default       -> throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // ── 내 판매 상품 목록 ─────────────────────────────────────

    public PageResponse<ProductResponse.Summary> getMyProducts(String sellerId, String statusStr, Pageable pageable) {
        Page<Product> page;

        if (statusStr != null && !statusStr.isBlank()) {
            // 특정 상태 필터
            ProductStatus status = EnumUtil.parse(ProductStatus.class, statusStr, ErrorCode.INVALID_INPUT_VALUE);
            page = productRepository.findBySellerMemberIdAndProductStatus(sellerId, status, pageable);
        } else {
            // 삭제된 상품 제외 전체
            page = productRepository.findBySellerMemberIdAndProductStatusNot(sellerId, ProductStatus.DELETED, pageable);
        }

        return toSummaryPage(page);
    }

    // ── 내부 유틸 메서드 ──────────────────────────────────────

    /** 존재하고 삭제되지 않은 상품 조회 */
    private Product findActiveProduct(Long productId) {
        return productRepository.findByProductIdAndProductStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Member findMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private ProductGroup findCategory(Long categoryId) {
        ProductGroup group = productGroupRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        if (!group.isCategory()) throw new CustomException(ErrorCode.INVALID_GROUP_TYPE);
        return group;
    }

    private ProductGroup findBrand(Long brandId) {
        ProductGroup group = productGroupRepository.findById(brandId)
                .orElseThrow(() -> new CustomException(ErrorCode.BRAND_NOT_FOUND));
        if (!group.isBrand()) throw new CustomException(ErrorCode.INVALID_GROUP_TYPE);
        return group;
    }

    /** 상품 소유자 검증 */
    private void validateOwner(Product product, String memberId) {
        if (!product.getSeller().getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.PRODUCT_UNAUTHORIZED);
        }
    }

    /** 판매자 프로필 카드 구성 */
    private ProductResponse.SellerInfo buildSellerInfo(Member seller) {
        long completedCount = orderRepository.countBySellerIdAndOrderStatus(
                seller.getMemberId(), "COMPLETED");
        Double avgRating = reviewRepository
                .findAverageRatingBySellerId(seller.getMemberId())
                .orElse(null);

        return new ProductResponse.SellerInfo(
                seller.getMemberId(),
                seller.getNickname(),
                seller.getSellerGrade(),
                completedCount,
                avgRating
        );
    }

    /** 이미지 목록 저장 (index = sort_order) */
    private void saveImages(Product product, List<String> imageUrls) {
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrls.get(i))
                    .sortOrder(i)
                    .build());
        }
        productImageRepository.saveAll(images);
    }

    /** 상품 ID 목록 → 대표 이미지(sort_order=0) 맵 (N+1 방지 배치 조회) */
    private Map<Long, String> buildThumbnailMap(List<Long> productIds) {
        if (productIds.isEmpty()) return Map.of();
        return productImageRepository.findThumbnailsByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getProductId(),
                        ProductImage::getImageUrl,
                        (existing, replacement) -> existing  // 중복 시 먼저 조회된 값 유지
                ));
    }

    /** Page<Product> → PageResponse<Summary> */
    private PageResponse<ProductResponse.Summary> toSummaryPage(Page<Product> page) {
        Map<Long, String> thumbnailMap = buildThumbnailMap(
                page.getContent().stream().map(Product::getProductId).toList());
        return PageResponse.of(page.map(p ->
                ProductResponse.Summary.from(p, thumbnailMap.get(p.getProductId()))));
    }

    /** List<Product> → List<Summary> (홈 화면용) */
    private List<ProductResponse.Summary> toSummaryList(List<Product> products) {
        Map<Long, String> thumbnailMap = buildThumbnailMap(
                products.stream().map(Product::getProductId).toList());
        return products.stream()
                .map(p -> ProductResponse.Summary.from(p, thumbnailMap.get(p.getProductId())))
                .toList();
    }
}

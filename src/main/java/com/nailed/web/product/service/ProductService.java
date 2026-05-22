package com.nailed.web.product.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.common.enums.ProductCondition;
import com.nailed.common.enums.ProductStatus;
import com.nailed.common.enums.SizeCode;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.Set;
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
            throw new CustomException(ErrorCode.PRODUCT_IMAGE_SIZE_EXCEEDED);
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

        // 사이즈 검증 (카테고리 타입에 맞는 사이즈인지 확인)
        if (req.size() != null && !req.size().isBlank()) {
            validateSize(req.size(), category);
        }

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

    // ── 상품 카드 클릭 → 상세 페이지 데이터 조회 ─────────────

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

        // 카테고리 전체 경로 (맨즈웨어 > 상의 > 티셔츠)
        String categoryPath = buildCategoryPath(product.getCategory());

        return ProductResponse.Detail.from(product, imageUrls, sellerInfo, categoryPath);
    }

    private String buildCategoryPath(ProductGroup category) {
        List<String> parts = new ArrayList<>();
        ProductGroup curr = category;
        while (curr != null) {
            parts.add(0, curr.getName());
            curr = curr.getParent();
        }
        return String.join(" > ", parts);
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

    // ── 카테고리 코드 prefix 목록 (MENS → MENS 하위 전체) ────

    public PageResponse<ProductResponse.Summary> getListByCode(String categoryCode, Pageable pageable) {
        Page<Product> page = productRepository
                .findByCategoryCodePrefixAndProductStatusNot(categoryCode + "%", ProductStatus.DELETED, pageable);
        return toSummaryPage(page);
    }

    // ── 검색 + 다중 필터 ──────────────────────────────────────

    public PageResponse<ProductResponse.Summary> search(Long categoryId, String keyword,
                                                        Integer minPrice, Integer maxPrice,
                                                        String conditionCode, String size,
                                                        String sortBy, Pageable pageable) {
        ProductCondition condition = (conditionCode != null && !conditionCode.isBlank())
                ? EnumUtil.parse(ProductCondition.class, conditionCode, ErrorCode.INVALID_INPUT_VALUE)
                : null;

        Page<Product> page;
        if ("popular".equals(sortBy)) {
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            page = productRepository.searchOrderByPopular(
                    ProductStatus.ON_SALE, categoryId, keyword, minPrice, maxPrice, condition, size, unsorted);
        } else {
            Sort sort = switch (sortBy) {
                case "price_asc"  -> Sort.by("price").ascending();
                case "price_desc" -> Sort.by("price").descending();
                default           -> Sort.by("createdAt").descending();
            };
            Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            page = productRepository.search(
                    ProductStatus.ON_SALE, categoryId, keyword, minPrice, maxPrice, condition, size, sorted);
        }

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

        ProductGroup category = findCategory(req.categoryId());
        ProductGroup brand = req.brandId() != null ? findBrand(req.brandId()) : null;
        ProductCondition condition = EnumUtil.parse(ProductCondition.class, req.conditionCode(), ErrorCode.INVALID_INPUT_VALUE);

        if (req.size() != null && !req.size().isBlank()) {
            validateSize(req.size(), category);
        }

        product.update(req.title(), category, brand, req.price(),
                req.description(), condition, req.size(), req.hashtags());

        syncImages(product, req.imageUrls());
    }

    // ── 상품 삭제 (소프트) ────────────────────────────────────

    @Transactional
    public void delete(Long productId, String sellerId, String reason) {
        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

        // 진행중 거래가 있으면 삭제 불가 (REQUESTED~DELIVERED 상태)
        if (orderRepository.existsByProductIdAndOrderStatusIn(
                productId, List.of("REQUESTED", "PAID", "SHIPPING"))) {
            throw new CustomException(ErrorCode.PRODUCT_HAS_ACTIVE_ORDER);
        }

        product.delete(reason);
    }

    // ── 판매 상태 변경 ────────────────────────────────────────

    @Transactional
    public void changeStatus(Long productId, String sellerId, ProductStatus newStatus) {
        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

        // DELETED는 delete API로만 처리
        if (newStatus == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 현재 상태 그대로면 변경 불필요
        if (product.getProductStatus() == newStatus) {
            return;
        }

        switch (newStatus) {
            case ON_SALE -> product.restore();
            case SOLD    -> product.completeSale();
            default      -> throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // ── 사이즈 검증 ──────────────────────────────────────────

    private void validateSize(String size, ProductGroup category) {
        String code = category.getCode();
        boolean isShoe = code.contains("_SHOES_");
        boolean isClothing = code.contains("_TOP_") || code.contains("_OUTER_")
                || code.contains("_BOTTOM_") || code.contains("_SKIRT_") || code.contains("_DRESS_");

        // 신발·의류 외 기타 카테고리(주얼리, IT기기 등)는 사이즈 형식 검증 없음
        if (!isShoe && !isClothing) return;

        SizeCode sizeCode = SizeCode.fromValue(size);
        if (sizeCode == null) {
            throw new CustomException(ErrorCode.INVALID_SIZE);
        }
        if (isShoe && sizeCode.getSizeType() != SizeCode.SizeType.SHOES) {
            throw new CustomException(ErrorCode.INVALID_SIZE);
        }
        if (isClothing && sizeCode.getSizeType() != SizeCode.SizeType.CLOTHING) {
            throw new CustomException(ErrorCode.INVALID_SIZE);
        }
    }

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

    /** 판매자 프로필 구성 */
    private ProductResponse.SellerInfo buildSellerInfo(Member seller) {
    	long completedCount = orderRepository.countBySellerIdAndOrderStatus(
    	        seller.getMemberId(), OrderStatus.DELIVERED.name());
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

    private void syncImages(Product product, List<String> newUrls) {
        List<ProductImage> currentImages = product.getImages();

        // 새 목록에 없는 이미지 삭제 (orphanRemoval이 DB DELETE 처리)
        currentImages.removeIf(img -> !newUrls.contains(img.getImageUrl()));

        // 기존에 남아있는 이미지 URL 세트
        Set<String> existingUrls = currentImages.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toSet());

        // 새로 추가된 이미지만 INSERT (최종 순서 기준으로 sort_order 설정)
        List<ProductImage> toAdd = new ArrayList<>();
        for (String url : newUrls) {
            if (!existingUrls.contains(url)) {
                toAdd.add(ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .sortOrder(newUrls.indexOf(url))
                        .build());
            }
        }
        productImageRepository.saveAll(toAdd);

        // 기존 이미지 sort_order 갱신 (순서 변경 반영)
        for (ProductImage img : currentImages) {
            img.updateSortOrder(newUrls.indexOf(img.getImageUrl()));
        }
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

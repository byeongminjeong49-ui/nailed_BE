package com.nailed.web.product.service;

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
import com.nailed.web.product.entity.ProductPrdSequence;
import com.nailed.web.product.repository.ProductGroupRepository;
import com.nailed.web.product.repository.ProductImageRepository;
import com.nailed.web.product.repository.ProductPrnSequenceRepository;
import com.nailed.web.product.repository.ProductRepository;
import com.nailed.web.review.repository.ReviewRepository;
import com.nailed.web.wishlist.repository.WishlistRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final String MENS_CATEGORY_CODE = "MENS";
    private static final String WOMENS_CATEGORY_CODE = "WOMENS";

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductPrnSequenceRepository prnSequenceRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.static.product.path:src/main/resources/static/images/products}")
    private String staticProductPath;

    // ── 이미지 업로드 ─────────────────────────────────────────

    public String uploadImage(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!List.of("jpg", "jpeg", "png", "webp").contains(ext)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new CustomException(ErrorCode.PRODUCT_IMAGE_SIZE_EXCEEDED);
        }

        try {
            // 임시 UUID 파일명으로 uploads/ 에 저장 (register 시 PRD 네이밍으로 교체됨)
            String tempFileName = UUID.randomUUID() + "." + ext;
            Path targetPath = Paths.get(uploadPath, tempFileName);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + tempFileName;
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
                .shippingFee(req.shippingFee())
                .description(req.description())
                .conditionCode(condition)
                .size(req.size())
                .hashtags(req.hashtags())
                .build();

        Product saved = productRepository.save(product);

        // PRD 순번 채번 (product_prn_sequence 테이블 INSERT → AUTO_INCREMENT 값 사용)
        int prdNumber = prnSequenceRepository.save(new ProductPrdSequence()).getSeqId();

        // 임시 UUID 파일을 PRD 시퀀스 파일명으로 교체 후 이미지 저장
        List<String> finalUrls = renameToSequence(req.imageUrls(), prdNumber);
        saveImages(saved, finalUrls);

        return saved.getProductId();
    }

    // ── 상품 카드 클릭 → 상세 페이지 데이터 조회 ─────────────

    public ProductResponse.Detail getDetail(Long productId, String memberId) {
        Product product = findActiveProduct(productId);

        List<ProductImage> imgList = productImageRepository.findByProductProductIdOrderBySortOrderAsc(productId);
        List<String> imageUrls = new ArrayList<>();
        for (ProductImage img : imgList) {
            imageUrls.add(img.getImageUrl());
        }

        // 판매자 프로필 카드 구성
        ProductResponse.SellerInfo sellerInfo = buildSellerInfo(product.getSeller());

        // 카테고리 전체 경로 (맨즈웨어 > 상의 > 티셔츠)
        String categoryPath = buildCategoryPath(product.getCategory());

        // 현재 로그인 유저의 찜 여부 (비로그인이면 false)
        boolean isWishlisted = memberId != null &&
                wishlistRepository.existsByMemberMemberIdAndProductProductId(memberId, productId);

        return ProductResponse.Detail.from(product, imageUrls, sellerInfo, categoryPath, isWishlisted);
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

    public PageResponse<ProductResponse.Summary> getList(Long categoryId,
                                                          Integer minPrice, Integer maxPrice,
                                                          String gender, boolean excludeSold,
                                                          String productSize, String conditionCode,
                                                          String sortBy, Pageable pageable) {
        ProductCondition condition = parseCondition(conditionCode);
        String genderCodePrefix = resolveGenderCategoryCode(gender);
        Page<Product> page = getCategoryFilteredPage(categoryId, null, minPrice, maxPrice,
                genderCodePrefix, excludeSold, productSize, condition, sortBy, pageable);
        return toSummaryPage(page);
    }

    // ── 카테고리 코드 prefix 목록 (MENS → MENS 하위 전체) ────

    public PageResponse<ProductResponse.Summary> getListByCode(String categoryCode,
                                                               Integer minPrice, Integer maxPrice,
                                                               String gender, boolean excludeSold,
                                                               String productSize, String conditionCode,
                                                               String sortBy, Pageable pageable) {
        ProductCondition condition = parseCondition(conditionCode);
        String genderCodePrefix = resolveGenderCategoryCode(gender);
        Page<Product> page = getCategoryFilteredPage(null, categoryCode, minPrice, maxPrice,
                genderCodePrefix, excludeSold, productSize, condition, sortBy, pageable);
        return toSummaryPage(page);
    }

    // ── 검색 + 다중 필터 ──────────────────────────────────────

    public PageResponse<ProductResponse.Summary> search(Long categoryId, String keyword,
                                                        Integer minPrice, Integer maxPrice,
                                                        String conditionCode, String productSize,
                                                        String gender, boolean excludeSold,
                                                        String sortBy, Pageable pageable) {
        ProductCondition condition = parseCondition(conditionCode);
        String normalizedKeyword = normalizeKeyword(keyword);
        String categoryCodePrefix = resolveGenderCategoryCode(gender);

        Page<Product> page;
        if ("popular".equals(sortBy)) {
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            page = productRepository.searchOrderByPopular(
                    ProductStatus.ON_SALE, ProductStatus.SOLD, excludeSold, categoryId, categoryCodePrefix,
                    normalizedKeyword, minPrice, maxPrice, condition, productSize, unsorted);
        } else {
            Sort sort;
            if ("price_asc".equals(sortBy)) {
                sort = Sort.by("price").ascending();
            } else if ("price_desc".equals(sortBy)) {
                sort = Sort.by("price").descending();
            } else {
                sort = Sort.by("createdAt").descending();
            }
            Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            page = productRepository.search(
                    ProductStatus.ON_SALE, ProductStatus.SOLD, excludeSold, categoryId, categoryCodePrefix,
                    normalizedKeyword, minPrice, maxPrice, condition, productSize, sorted);
        }

        return toSummaryPage(page);
    }

    private Page<Product> getCategoryFilteredPage(Long categoryId, String categoryCode,
                                                  Integer minPrice, Integer maxPrice,
                                                  String genderCodePrefix, boolean excludeSold,
                                                  String productSize, ProductCondition condition,
                                                  String sortBy, Pageable pageable) {
        String categoryCodePrefix = normalizeKeyword(categoryCode);

        if ("popular".equals(sortBy)) {
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            return productRepository.findCategoryProductsOrderByPopular(
                    ProductStatus.ON_SALE, ProductStatus.SOLD, excludeSold, categoryId, categoryCodePrefix,
                    genderCodePrefix, minPrice, maxPrice, condition, productSize, unsorted);
        }

        Sort sort;
        if ("price_asc".equals(sortBy)) {
            sort = Sort.by("price").ascending();
        } else if ("price_desc".equals(sortBy)) {
            sort = Sort.by("price").descending();
        } else {
            sort = Sort.by("createdAt").descending();
        }
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return productRepository.findCategoryProducts(
                ProductStatus.ON_SALE, ProductStatus.SOLD, excludeSold, categoryId, categoryCodePrefix,
                genderCodePrefix, minPrice, maxPrice, condition, productSize, sorted);
    }

    private ProductCondition parseCondition(String conditionCode) {
        return (conditionCode != null && !conditionCode.isBlank())
                ? EnumUtil.parse(ProductCondition.class, conditionCode, ErrorCode.INVALID_INPUT_VALUE)
                : null;
    }

    private String normalizeKeyword(String keyword) {
        return (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
    }

    private String resolveGenderCategoryCode(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        String g = gender.trim().toUpperCase();
        if ("MENS".equals(g) || "MEN".equals(g) || "MALE".equals(g)) {
            return MENS_CATEGORY_CODE;
        } else if ("WOMENS".equals(g) || "WOMEN".equals(g) || "FEMALE".equals(g)) {
            return WOMENS_CATEGORY_CODE;
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // ── 판매자의 다른 상품 최대 5개 ──────────────────────────

    public List<ProductResponse.Summary> getSellerProducts(String sellerId, Long excludeId) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<Product> products = productRepository.findSellerProducts(sellerId, excludeId, ProductStatus.DELETED, pageable);
        return toSummaryList(products);
    }

    // ── 같은 카테고리 "비슷한 상품" 최대 N개 ────────────────

    public List<ProductResponse.Summary> getRelatedProducts(Long productId, int size) {
        Product product = findActiveProduct(productId);
        Pageable pageable = PageRequest.of(0, size);
        List<Product> products = productRepository.findRelatedProducts(
                product.getCategory().getGroupId(), productId, ProductStatus.ON_SALE, pageable);
        return toSummaryList(products);
    }

    // ── 홈 추천: 최신 6개 ────────────────────────────────────

    public List<ProductResponse.Summary> getNewProducts() {
        List<Product> products = productRepository
                .findTop6ByProductStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE);
        return toSummaryList(products);
    }

    // ── 홈 인기 TOP 5 ─────────────────────────────────────────

    public List<ProductResponse.Summary> getPopularProducts() {
        List<Product> products = productRepository.findPopularTop5(ProductStatus.ON_SALE.name());
        return toSummaryList(products);
    }

    // ── 메인 카테고리 랜덤 10개 (상세 페이지 하단) ───────────

    public List<ProductResponse.Summary> getRandomProducts(int size) {
        List<Product> products = productRepository.findRandomProducts();
        List<Product> sliced = products.size() > size ? products.subList(0, size) : products;
        return toSummaryList(sliced);
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

        product.update(req.title(), category, brand, req.price(), req.shippingFee(),
                req.description(), condition, req.size(), req.hashtags());

        // 새로 업로드된 임시 파일만 PRD 시퀀스로 교체
        List<String> finalUrls = renameNewUploads(req.imageUrls(), product.getProductId());
        syncImages(product, finalUrls);
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

        if (newStatus == ProductStatus.ON_SALE) {
            product.restore();
        } else if (newStatus == ProductStatus.SOLD) {
            product.completeSale();
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
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
        if (!group.isValidBrandRef()) throw new CustomException(ErrorCode.INVALID_GROUP_TYPE);
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
        long reviewCount = reviewRepository.countByOrderSellerId(seller.getMemberId());
        Double avgRating = reviewRepository
                .findAverageRatingBySellerId(seller.getMemberId())
                .orElse(null);
        Optional<String> profileImageOpt = memberRepository.findProfileImageUrlByMemberId(seller.getMemberId());
        String profileImageUrl = null;
        if (profileImageOpt.isPresent() && !profileImageOpt.get().isBlank()) {
            profileImageUrl = profileImageOpt.get();
        }

        return new ProductResponse.SellerInfo(
                seller.getMemberId(),
                seller.getNickname(),
                seller.getSellerGrade(),
                reviewCount,
                avgRating,
                profileImageUrl
        );
    }

    /**
     * 상품 등록 시: 임시 UUID 파일 전체를 PRD_{productId}_{1,2,3...}.jpg 로 rename 후 static 경로로 이동
     * sort_order 0 = 대표 이미지 (첫 번째 URL)
     */
    private List<String> renameToSequence(List<String> tempUrls, int prdNumber) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < tempUrls.size(); i++) {
            String tempUrl = tempUrls.get(i);
            String ext = tempUrl.substring(tempUrl.lastIndexOf("."));
            String newName = String.format("PRD_%03d_%d%s", prdNumber, i + 1, ext);

            try {
                Path from = Paths.get(uploadPath).resolve(tempUrl.replace("/uploads/", ""));
                Path to   = Paths.get(staticProductPath).resolve(newName);
                Files.createDirectories(to.getParent());
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            result.add("/images/products/" + newName);
        }
        return result;
    }

    /**
     * 상품 수정 시: 기존 PRD URL은 그대로 두고, 새로 업로드된 임시 파일만 PRD 시퀀스로 rename
     * 기존 이미지의 max 순번 다음부터 이어서 채번
     */
    private List<String> renameNewUploads(List<String> imageUrls, Long productId) {
        List<ProductImage> existingImages = productImageRepository.findByProductProductIdOrderBySortOrderAsc(productId);
        int maxIndex = 0;
        for (ProductImage img : existingImages) {
            if (!img.getImageUrl().startsWith("/images/products/")) continue;
            String name = img.getImageUrl().substring(img.getImageUrl().lastIndexOf('/') + 1);
            String[] parts = name.split("[_.]");
            try {
                int idx = Integer.parseInt(parts[parts.length - 2]);
                if (idx > maxIndex) maxIndex = idx;
            } catch (Exception e) {
                // ignore malformed filename
            }
        }

        List<String> result = new ArrayList<>();
        int newIdx = 0;
        for (String url : imageUrls) {
            if (!url.startsWith("/uploads/")) {
                result.add(url);
                continue;
            }
            newIdx++;
            String ext = url.substring(url.lastIndexOf("."));
            String newName = String.format("PRD_%03d_%d%s", productId, maxIndex + newIdx, ext);

            try {
                Path from = Paths.get(uploadPath).resolve(url.replace("/uploads/", ""));
                Path to   = Paths.get(staticProductPath).resolve(newName);
                Files.createDirectories(to.getParent());
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            result.add("/images/products/" + newName);
        }
        return result;
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
        product.getImages().clear(); // orphanRemoval이 기존 이미지 전체 DELETE 처리
        saveImages(product, newUrls);
    }

    private Map<Long, String> buildThumbnailMap(List<Long> productIds) {
        if (productIds.isEmpty()) return Map.of();
        List<ProductImage> thumbnails = productImageRepository.findThumbnailsByProductIds(productIds);
        Map<Long, String> map = new HashMap<>();
        for (ProductImage img : thumbnails) {
            Long pid = img.getProduct().getProductId();
            if (!map.containsKey(pid)) {
                map.put(pid, img.getImageUrl());
            }
        }
        return map;
    }

    private PageResponse<ProductResponse.Summary> toSummaryPage(Page<Product> page) {
        List<Long> productIds = new ArrayList<>();
        for (Product p : page.getContent()) {
            productIds.add(p.getProductId());
        }
        Map<Long, String> thumbnailMap = buildThumbnailMap(productIds);
        return PageResponse.of(page.map(p ->
                ProductResponse.Summary.from(p, thumbnailMap.get(p.getProductId()))));
    }

    private List<ProductResponse.Summary> toSummaryList(List<Product> products) {
        List<Long> productIds = new ArrayList<>();
        for (Product p : products) {
            productIds.add(p.getProductId());
        }
        Map<Long, String> thumbnailMap = buildThumbnailMap(productIds);
        List<ProductResponse.Summary> result = new ArrayList<>();
        for (Product p : products) {
            result.add(ProductResponse.Summary.from(p, thumbnailMap.get(p.getProductId())));
        }
        return result;
    }
}

package com.nailed.web.review.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.entity.ProductImage;
import com.nailed.web.product.repository.ProductImageRepository;
import com.nailed.web.product.repository.ProductRepository;
import com.nailed.web.review.dto.ReviewRequest;
import com.nailed.web.review.dto.ReviewResponse;
import com.nailed.web.review.entity.Review;
import com.nailed.web.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 리뷰 작성
     * - 배송 완료(DELIVERED) 상태의 주문에 대해서만 작성 가능
     * - 주문의 구매자 본인만 작성 가능
     * - 주문당 1개 제한 (UNIQUE 제약 + 사전 중복 체크)
     */
    @Transactional
    public ReviewResponse.Detail write(String buyerId, ReviewRequest.Write req) {
        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!"DELIVERED".equals(order.getOrderStatus())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (reviewRepository.existsByOrderOrderId(req.orderId())) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = Review.builder()
                .order(order)
                .buyer(buyer)
                .rating(req.rating())
                .content(req.content())
                .build();

        // write는 단건이므로 단건 변환 메서드 사용
        return toDetail(reviewRepository.save(review));
    }

    /**
     * 판매자 리뷰 목록 조회
     * - 평균 별점과 리뷰 페이지를 함께 반환
     * - 비로그인 사용자도 조회 가능
     */
    public ReviewResponse.SellerReviews getSellerReviews(String sellerId, Pageable pageable) {
        if (!memberRepository.existsById(sellerId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Double averageRating = reviewRepository
                .findAverageRatingBySellerId(sellerId)
                .orElse(null);

        Page<Review> page = reviewRepository
                .findByOrderSellerIdOrderByCreatedAtDesc(sellerId, pageable);

        // 상품 ID 일괄 수집 → 상품 정보 + 썸네일 배치 조회 (N+1 방지)
        List<Long> productIds = new ArrayList<>();
        for (Review r : page.getContent()) {
            if (r.getOrder().getProductId() != null) {
                productIds.add(r.getOrder().getProductId());
            }
        }

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(productIds);
            for (Product p : products) {
                productMap.put(p.getProductId(), p);
            }
        }
        Map<Long, String> thumbnailMap = buildThumbnailMap(productIds);

        Page<ReviewResponse.Detail> detailPage = page.map(r ->
                toDetail(r, productMap, thumbnailMap));

        return new ReviewResponse.SellerReviews(averageRating, PageResponse.of(detailPage));
    }

    /**
     * Review → Detail DTO 변환 (단건용, write 직후 호출)
     * - 상품 1개만 조회하므로 배치 없이 직접 조회
     */
    private ReviewResponse.Detail toDetail(Review review) {
        Long productId = review.getOrder().getProductId();

        String productTitle = null;
        Long price = null;
        String productImageUrl = null;

        if (productId != null) {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                productTitle = product.getTitle();
                price = Long.valueOf(product.getPrice());
            }

            List<ProductImage> images = productImageRepository
                    .findThumbnailsByProductIds(List.of(productId));
            if (!images.isEmpty()) {
                productImageUrl = images.get(0).getImageUrl();
            }
        }

        return new ReviewResponse.Detail(
                review.getReviewId(),
                review.getOrder().getOrderId(),
                review.getBuyer().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                productTitle,
                productImageUrl,
                price
        );
    }

    /**
     * Review → Detail DTO 변환 (배치용, 미리 조회한 맵 활용)
     * - getSellerReviews에서 페이지 전체를 한 번에 변환할 때 사용
     * - 상품 조회가 맵 참조로 대체되므로 N+1 쿼리 없음
     */
    private ReviewResponse.Detail toDetail(Review review,
                                            Map<Long, Product> productMap,
                                            Map<Long, String> thumbnailMap) {
        Long productId = review.getOrder().getProductId();

        String productTitle = null;
        Long price = null;
        String productImageUrl = null;

        if (productId != null) {
            Product product = productMap.get(productId);
            if (product != null) {
                productTitle = product.getTitle();
                price = Long.valueOf(product.getPrice());
            }
            productImageUrl = thumbnailMap.get(productId);
        }

        return new ReviewResponse.Detail(
                review.getReviewId(),
                review.getOrder().getOrderId(),
                review.getBuyer().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                productTitle,
                productImageUrl,
                price
        );
    }

    /** 썸네일 배치 조회 (productId → imageUrl 맵 반환) */
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
}

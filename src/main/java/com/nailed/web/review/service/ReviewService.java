package com.nailed.web.review.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.review.dto.ReviewRequest;
import com.nailed.web.review.dto.ReviewResponse;
import com.nailed.web.review.entity.Review;
import com.nailed.web.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    /**
     * 리뷰 작성
     * - 배송 완료(DELIVERED) 상태의 주문에 대해서만 작성 가능
     * - 주문의 구매자 본인만 작성 가능
     * - 주문당 1개 제한 (UNIQUE 제약 + 사전 중복 체크)
     */
    @Transactional
    public ReviewResponse.Detail write(String buyerId, ReviewRequest.Write req) {
        // 리뷰 가능 여부 검증 (배송 완료 주문 + 해당 구매자 본인 + 중복 방지)
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

        return ReviewResponse.Detail.from(reviewRepository.save(review));
    }

    /**
     * 판매자 리뷰 목록 조회
     * - 평균 별점과 리뷰 페이지를 함께 반환
     * - 비로그인 사용자도 조회 가능
     */
    public ReviewResponse.SellerReviews getSellerReviews(String sellerId, Pageable pageable) {
        // 판매자 존재 확인
        if (!memberRepository.existsById(sellerId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Double averageRating = reviewRepository
                .findAverageRatingBySellerId(sellerId)
                .orElse(null);

        Page<ReviewResponse.Detail> reviewPage = reviewRepository
                .findByOrderSellerIdOrderByCreatedAtDesc(sellerId, pageable)
                .map(ReviewResponse.Detail::from);

        return new ReviewResponse.SellerReviews(averageRating, PageResponse.of(reviewPage));
    }
}

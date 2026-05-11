package com.nailed.domain.review.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import com.nailed.domain.order.entity.Order;
import com.nailed.domain.order.repository.OrderRepository;
import com.nailed.domain.review.dto.ReviewRequest;
import com.nailed.domain.review.dto.ReviewResponse;
import com.nailed.domain.review.entity.Review;
import com.nailed.domain.review.repository.ReviewRepository;
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

    @Transactional
    public Long write(String buyerId, ReviewRequest.Write request) {
        if (reviewRepository.existsByOrderOrderId(request.orderId())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = Review.builder()
                .order(order)
                .buyer(buyer)
                .rating(request.rating())
                .content(request.content())
                .build();

        return reviewRepository.save(review).getReviewId();
    }

    public Page<ReviewResponse.Detail> getSellerReviews(String sellerId, Pageable pageable) {
        return reviewRepository.findBySellerMemberId(sellerId, pageable)
                .map(ReviewResponse.Detail::from);
    }
}

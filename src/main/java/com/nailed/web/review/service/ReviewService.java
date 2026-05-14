package com.nailed.web.review.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.util.SecurityUtil;
import com.nailed.web.review.dto.ReviewRequest;
import com.nailed.web.review.dto.ReviewResponse;
import com.nailed.web.review.entity.Review;
import com.nailed.web.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public Long writeReview(ReviewRequest.Write request) {
        Long reviewerId = SecurityUtil.getCurrentMemberId();

        if (reviewRepository.existsByOrderId(request.orderId())) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        if (request.rating() < 1 || request.rating() > 5) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return reviewRepository.save(
                new Review(request.orderId(), reviewerId, request.sellerId(), request.rating(), request.content())
        ).getReviewId();
    }

    public Page<ReviewResponse.Item> getReviewsBySeller(Long sellerId, int page, int size) {
        return reviewRepository.findBySellerId(
                sellerId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        ).map(ReviewResponse.Item::from);
    }
}

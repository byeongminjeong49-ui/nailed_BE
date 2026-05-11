package com.nailed.domain.review.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.domain.review.dto.ReviewRequest;
import com.nailed.domain.review.dto.ReviewResponse;
import com.nailed.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> write(
            @RequestParam String buyerId,
            @Valid @RequestBody ReviewRequest.Write request) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.write(buyerId, request)));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse.Detail>>> getSellerReviews(
            @PathVariable String sellerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getSellerReviews(sellerId, pageable)));
    }
}

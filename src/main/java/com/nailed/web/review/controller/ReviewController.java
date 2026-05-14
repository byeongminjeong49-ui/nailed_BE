package com.nailed.web.review.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.review.dto.ReviewRequest;
import com.nailed.web.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<Long> writeReview(@RequestBody ReviewRequest.Write request) {
        return ApiResponse.success(reviewService.writeReview(request));
    }
}

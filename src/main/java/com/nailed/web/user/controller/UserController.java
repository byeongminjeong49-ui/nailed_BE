package com.nailed.web.user.controller;

import com.nailed.common.response.ApiResponse;
import com.nailed.web.review.dto.ReviewResponse;
import com.nailed.web.review.service.ReviewService;
import com.nailed.web.user.dto.UserResponse;
import com.nailed.web.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    // GET /api/users/{userId}
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse.Profile> getProfile(@PathVariable Long userId) {
        return ApiResponse.success(userService.getProfile(userId));
    }

    // GET /api/users/{userId}/reviews?page=0&size=10
    @GetMapping("/{userId}/reviews")
    public ApiResponse<Page<ReviewResponse.Item>> getReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(reviewService.getReviewsBySeller(userId, page, size));
    }
}

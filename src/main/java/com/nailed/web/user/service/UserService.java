package com.nailed.web.user.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.common.enums.SellerGrade;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.review.repository.ReviewRepository;
import com.nailed.web.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    // 판매자 프로필 조회
    public UserResponse.Profile getProfile(Long userId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        long completedCount = orderRepository
                .countBySellerIdAndStatusIn(userId, List.of(OrderStatus.COMPLETED));

        SellerGrade grade = resolveGrade(completedCount);
        double avgRating = reviewRepository.findAvgRatingBySellerId(userId);

        return UserResponse.Profile.of(member, grade, avgRating, completedCount);
    }

    private SellerGrade resolveGrade(long completedCount) {
        if (completedCount >= 50) return SellerGrade.DIAMOND;
        if (completedCount >= 20) return SellerGrade.GOLD;
        if (completedCount >= 5)  return SellerGrade.SILVER;
        return SellerGrade.BRONZE;
    }
}

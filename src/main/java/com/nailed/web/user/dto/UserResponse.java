package com.nailed.web.user.dto;

import com.nailed.common.enums.SellerGrade;
import com.nailed.web.member.entity.Member;

public class UserResponse {

    public record Profile(
            Long userId,
            String nickname,
            String sellerGrade,
            double avgRating,
            long completedOrderCount
    ) {
        public static Profile of(Member member, SellerGrade grade, double avgRating, long completedOrderCount) {
            return new Profile(
                    member.getId(), member.getNickname(),
                    grade.name(), avgRating, completedOrderCount
            );
        }
    }
}

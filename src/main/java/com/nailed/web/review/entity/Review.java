package com.nailed.web.review.entity;

import com.nailed.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String content;

    public Review(Long orderId, Long reviewerId, Long sellerId, int rating, String content) {
        this.orderId = orderId;
        this.reviewerId = reviewerId;
        this.sellerId = sellerId;
        this.rating = rating;
        this.content = content;
    }
}

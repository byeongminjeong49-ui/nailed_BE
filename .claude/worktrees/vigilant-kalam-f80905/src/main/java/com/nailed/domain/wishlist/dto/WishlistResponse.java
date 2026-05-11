package com.nailed.domain.wishlist.dto;

import com.nailed.domain.wishlist.entity.Wishlist;

import java.time.LocalDateTime;

public class WishlistResponse {

    public record Item(
            Long wishlistId,
            Long productId,
            String productTitle,
            int productPrice,
            String productStatus,
            String thumbnailUrl,
            LocalDateTime createdAt
    ) {
        public static Item from(Wishlist wishlist) {
            String thumbnail = wishlist.getProduct().getImages().isEmpty()
                    ? null
                    : wishlist.getProduct().getImages().get(0).getImageUrl();
            return new Item(
                    wishlist.getWishlistId(),
                    wishlist.getProduct().getProductId(),
                    wishlist.getProduct().getTitle(),
                    wishlist.getProduct().getPrice(),
                    wishlist.getProduct().getStatus(),
                    thumbnail,
                    wishlist.getCreatedAt()
            );
        }
    }
}

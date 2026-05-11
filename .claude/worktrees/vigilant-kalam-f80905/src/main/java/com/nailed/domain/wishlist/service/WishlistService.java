package com.nailed.domain.wishlist.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import com.nailed.domain.product.entity.Product;
import com.nailed.domain.product.repository.ProductRepository;
import com.nailed.domain.wishlist.dto.WishlistResponse;
import com.nailed.domain.wishlist.entity.Wishlist;
import com.nailed.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void add(String memberId, Long productId) {
        if (wishlistRepository.existsByMemberMemberIdAndProductProductId(memberId, productId)) {
            throw new BusinessException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        wishlistRepository.save(Wishlist.builder()
                .member(member)
                .product(product)
                .build());

        product.increaseWishlistCount();
    }

    @Transactional
    public void remove(String memberId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByMemberMemberIdAndProductProductId(memberId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND));

        wishlistRepository.delete(wishlist);
        wishlist.getProduct().decreaseWishlistCount();
    }

    public Page<WishlistResponse.Item> getMyWishlist(String memberId, Pageable pageable) {
        return wishlistRepository.findByMemberMemberId(memberId, pageable)
                .map(WishlistResponse.Item::from);
    }
}

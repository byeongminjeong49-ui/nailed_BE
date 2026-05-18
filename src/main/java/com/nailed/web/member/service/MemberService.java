package com.nailed.web.member.service;

import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.member.dto.MemberRequest;
import com.nailed.web.member.dto.MemberResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MemberResponse.Home getUserHome(String memberId) {
        MemberResponse.Profile profile = getProfile(memberId);
        long sellingProductCount = count("""
                SELECT COUNT(*) FROM products
                WHERE seller_id = :memberId AND product_status NOT IN ('DELETED', 'SOLD')
                """, memberId);
        long soldProductCount = count("""
                SELECT COUNT(*) FROM products
                WHERE seller_id = :memberId AND product_status = 'SOLD'
                """, memberId);
        long wishlistCount = count("""
                SELECT COUNT(*) FROM wishlists
                WHERE member_id = :memberId
                """, memberId);
        long buyingOrderCount = count("""
                SELECT COUNT(*) FROM orders
                WHERE buyer_id = :memberId
                """, memberId);
        long sellingOrderCount = count("""
                SELECT COUNT(*) FROM orders
                WHERE seller_id = :memberId
                """, memberId);

        return new MemberResponse.Home(
                profile,
                sellingProductCount,
                soldProductCount,
                wishlistCount,
                buyingOrderCount,
                sellingOrderCount
        );
    }

    public MemberResponse.Profile getProfile(String memberId) {
        Object[] row = findMemberProfileRow(memberId);
        return toProfile(row);
    }

    @Transactional
    public MemberResponse.Profile updateProfile(String memberId, MemberRequest.ProfileUpdate request) {
        ensureMemberExists(memberId);
        validateNickname(memberId, request.nickname());

        entityManager.createNativeQuery("""
                UPDATE members
                SET nickname = COALESCE(:nickname, nickname),
                    shop_info = COALESCE(:shopInfo, shop_info),
                    bank_code = COALESCE(:bankCode, bank_code),
                    account_number = COALESCE(:accountNumber, account_number),
                    depositor_name = COALESCE(:depositorName, depositor_name)
                WHERE member_id = :memberId
                """)
                .setParameter("nickname", blankToNull(request.nickname()))
                .setParameter("shopInfo", request.shopInfo())
                .setParameter("bankCode", blankToNull(request.bankCode()))
                .setParameter("accountNumber", blankToNull(request.accountNumber()))
                .setParameter("depositorName", blankToNull(request.depositorName()))
                .setParameter("memberId", memberId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();
        return getProfile(memberId);
    }

    public PageResponse<MemberResponse.ProductSummary> getMyProducts(String memberId, String status, Pageable pageable) {
        ensureMemberExists(memberId);

        String statusCondition = "";
        if (status != null && !status.isBlank()) {
            statusCondition = " AND p.product_status = :status";
        }

        String baseSql = """
                FROM products p
                LEFT JOIN product_images pi
                    ON pi.product_id = p.product_id AND pi.sort_order = 0
                WHERE p.seller_id = :memberId
                  AND p.product_status <> 'DELETED'
                """ + statusCondition;

        Query dataQuery = entityManager.createNativeQuery("""
                SELECT p.product_id, p.title, p.price, p.condition_code, p.product_status,
                       p.view_count, p.wishlist_count, pi.image_url, p.created_at
                """ + baseSql + " ORDER BY p.created_at DESC");
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + baseSql);

        setMemberAndStatus(dataQuery, memberId, status);
        setMemberAndStatus(countQuery, memberId, status);
        applyPage(dataQuery, pageable);

        List<MemberResponse.ProductSummary> content = rows(dataQuery).stream()
                .map(this::toProductSummary)
                .toList();
        return PageResponse.of(new PageImpl<>(content, pageable, number(countQuery.getSingleResult()).longValue()));
    }

    public PageResponse<MemberResponse.WishlistItem> getMyWishlist(String memberId, Pageable pageable) {
        ensureMemberExists(memberId);

        String baseSql = """
                FROM wishlists w
                JOIN products p ON p.product_id = w.product_id
                JOIN members s ON s.member_id = p.seller_id
                LEFT JOIN product_images pi
                    ON pi.product_id = p.product_id AND pi.sort_order = 0
                WHERE w.member_id = :memberId
                  AND p.product_status <> 'DELETED'
                """;

        Query dataQuery = entityManager.createNativeQuery("""
                SELECT w.wishlist_id, p.product_id, p.title, p.price, p.condition_code,
                       p.product_status, p.seller_id, s.nickname, pi.image_url, w.created_at
                """ + baseSql + " ORDER BY w.created_at DESC");
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + baseSql);

        dataQuery.setParameter("memberId", memberId);
        countQuery.setParameter("memberId", memberId);
        applyPage(dataQuery, pageable);

        List<MemberResponse.WishlistItem> content = rows(dataQuery).stream()
                .map(this::toWishlistItem)
                .toList();
        return PageResponse.of(new PageImpl<>(content, pageable, number(countQuery.getSingleResult()).longValue()));
    }

    @Transactional
    public void deleteWishlist(String memberId, Long productId) {
        ensureMemberExists(memberId);

        int deleted = entityManager.createNativeQuery("""
                DELETE FROM wishlists
                WHERE member_id = :memberId AND product_id = :productId
                """)
                .setParameter("memberId", memberId)
                .setParameter("productId", productId)
                .executeUpdate();

        if (deleted == 0) {
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }

        entityManager.createNativeQuery("""
                UPDATE products
                SET wishlist_count = GREATEST(wishlist_count - 1, 0)
                WHERE product_id = :productId
                """)
                .setParameter("productId", productId)
                .executeUpdate();
    }

    public PageResponse<MemberResponse.OrderSummary> getMyOrders(
            String memberId, String type, String status, Pageable pageable) {
        ensureMemberExists(memberId);

        boolean selling = "SELL".equalsIgnoreCase(type);
        String ownerColumn = selling ? "o.seller_id" : "o.buyer_id";
        String statusCondition = "";
        if (status != null && !status.isBlank()) {
            statusCondition = " AND o.order_status = :status";
        }

        String baseSql = """
                FROM orders o
                JOIN products p ON p.product_id = o.product_id
                LEFT JOIN product_images pi
                    ON pi.product_id = p.product_id AND pi.sort_order = 0
                WHERE """ + ownerColumn + " = :memberId" + statusCondition;

        Query dataQuery = entityManager.createNativeQuery("""
                SELECT o.order_id, o.product_id, p.title, pi.image_url, o.buyer_id, o.seller_id,
                       o.product_amount, o.shipping_fee, o.final_price, o.order_status,
                       o.cancel_request_status, o.created_at, o.paid_at, o.shipped_at,
                       o.delivered_at, o.completed_at, o.cancelled_at
                """ + baseSql + " ORDER BY o.created_at DESC");
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + baseSql);

        setMemberAndStatus(dataQuery, memberId, status);
        setMemberAndStatus(countQuery, memberId, status);
        applyPage(dataQuery, pageable);

        List<MemberResponse.OrderSummary> content = rows(dataQuery).stream()
                .map(this::toOrderSummary)
                .toList();
        return PageResponse.of(new PageImpl<>(content, pageable, number(countQuery.getSingleResult()).longValue()));
    }

    public PageResponse<MemberResponse.SettlementSummary> getMySettlements(
            String memberId, String status, Pageable pageable) {
        ensureMemberExists(memberId);

        String statusCondition = "";
        if (status != null && !status.isBlank()) {
            statusCondition = " AND o.order_status = :status";
        }

        String baseSql = """
                FROM orders o
                JOIN products p ON p.product_id = o.product_id
                WHERE o.seller_id = :memberId
                """ + statusCondition;

        Query dataQuery = entityManager.createNativeQuery("""
                SELECT o.order_id, o.product_id, p.title, o.commission, o.final_price,
                       o.seller_settlement_amount, o.order_status, o.completed_at, o.created_at
                """ + baseSql + " ORDER BY o.created_at DESC");
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + baseSql);

        setMemberAndStatus(dataQuery, memberId, status);
        setMemberAndStatus(countQuery, memberId, status);
        applyPage(dataQuery, pageable);

        List<MemberResponse.SettlementSummary> content = rows(dataQuery).stream()
                .map(this::toSettlementSummary)
                .toList();
        return PageResponse.of(new PageImpl<>(content, pageable, number(countQuery.getSingleResult()).longValue()));
    }

    @Transactional
    public void withdraw(String memberId) {
        ensureMemberExists(memberId);
        entityManager.createNativeQuery("""
                UPDATE members
                SET member_status = 'WITHDRAWN',
                    refresh_token = NULL,
                    refresh_token_expires_at = NULL
                WHERE member_id = :memberId
                """)
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

    private void ensureMemberExists(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private Object[] findMemberProfileRow(String memberId) {
        List<?> result = entityManager.createNativeQuery("""
                SELECT member_id, email, nickname, name, shop_info, member_status,
                       seller_grade, role, bank_code, account_number, depositor_name,
                       marketing_agreed, created_at
                FROM members
                WHERE member_id = :memberId
                """)
                .setParameter("memberId", memberId)
                .getResultList();

        if (result.isEmpty()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return (Object[]) result.get(0);
    }

    private void validateNickname(String memberId, String nickname) {
        String value = blankToNull(nickname);
        if (value == null) {
            return;
        }

        Number duplicated = number(entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM members
                WHERE nickname = :nickname AND member_id <> :memberId
                """)
                .setParameter("nickname", value)
                .setParameter("memberId", memberId)
                .getSingleResult());
        if (duplicated.longValue() > 0) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
    }

    private long count(String sql, String memberId) {
        return number(entityManager.createNativeQuery(sql)
                .setParameter("memberId", memberId)
                .getSingleResult()).longValue();
    }

    private void setMemberAndStatus(Query query, String memberId, String status) {
        query.setParameter("memberId", memberId);
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status);
        }
    }

    private void applyPage(Query query, Pageable pageable) {
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
    }

    private List<Object[]> rows(Query query) {
        List<?> raw = query.getResultList();
        List<Object[]> rows = new ArrayList<>();
        for (Object row : raw) {
            rows.add((Object[]) row);
        }
        return rows;
    }

    private MemberResponse.Profile toProfile(Object[] row) {
        return new MemberResponse.Profile(
                string(row[0]),
                string(row[1]),
                string(row[2]),
                string(row[3]),
                string(row[4]),
                string(row[5]),
                string(row[6]),
                string(row[7]),
                string(row[8]),
                string(row[9]),
                string(row[10]),
                bool(row[11]),
                time(row[12])
        );
    }

    private MemberResponse.ProductSummary toProductSummary(Object[] row) {
        return new MemberResponse.ProductSummary(
                number(row[0]).longValue(),
                string(row[1]),
                number(row[2]).intValue(),
                string(row[3]),
                string(row[4]),
                number(row[5]).intValue(),
                number(row[6]).intValue(),
                string(row[7]),
                time(row[8])
        );
    }

    private MemberResponse.WishlistItem toWishlistItem(Object[] row) {
        return new MemberResponse.WishlistItem(
                number(row[0]).longValue(),
                number(row[1]).longValue(),
                string(row[2]),
                number(row[3]).intValue(),
                string(row[4]),
                string(row[5]),
                string(row[6]),
                string(row[7]),
                string(row[8]),
                time(row[9])
        );
    }

    private MemberResponse.OrderSummary toOrderSummary(Object[] row) {
        return new MemberResponse.OrderSummary(
                string(row[0]),
                number(row[1]).longValue(),
                string(row[2]),
                string(row[3]),
                string(row[4]),
                string(row[5]),
                number(row[6]).intValue(),
                number(row[7]).intValue(),
                number(row[8]).intValue(),
                string(row[9]),
                string(row[10]),
                time(row[11]),
                time(row[12]),
                time(row[13]),
                time(row[14]),
                time(row[15]),
                time(row[16])
        );
    }

    private MemberResponse.SettlementSummary toSettlementSummary(Object[] row) {
        return new MemberResponse.SettlementSummary(
                string(row[0]),
                number(row[1]).longValue(),
                string(row[2]),
                number(row[3]).intValue(),
                number(row[4]).intValue(),
                number(row[5]).intValue(),
                string(row[6]),
                time(row[7]),
                time(row[8])
        );
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
    }

    private Number number(Object value) {
        return (Number) value;
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value != null && number(value).intValue() == 1;
    }

    private LocalDateTime time(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value : null;
    }
}

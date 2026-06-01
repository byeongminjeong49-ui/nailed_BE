package com.nailed.web.order.service;

import com.nailed.common.enums.OrderStatus;
import com.nailed.common.exception.CustomException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.common.response.PageResponse;
import com.nailed.web.member.entity.Member;
import com.nailed.web.member.repository.MemberRepository;
import com.nailed.web.order.dto.AdminOrderResponse;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.entity.ProductImage;
import com.nailed.web.product.repository.ProductImageRepository;
import com.nailed.web.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public PageResponse<AdminOrderResponse.Summary> getOrders(
            String keyword,
            String orderStatus,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {
        var page = orderRepository.searchAdminOrders(
                blankToNull(keyword),
                parseOrderStatus(orderStatus),
                dateFrom != null ? dateFrom.atStartOfDay() : null,
                dateTo != null ? dateTo.atTime(LocalTime.MAX) : null,
                pageable
        );

        List<Order> orders = page.getContent();
        Map<String, Member> memberMap = buildMemberMap(orders);
        Map<Long, Product> productMap = buildProductMap(orders);
        Map<Long, String> thumbnailMap = buildThumbnailMap(productMap.keySet());

        return PageResponse.of(page.map(order ->
                toSummary(order, memberMap, productMap, thumbnailMap)));
    }

    private AdminOrderResponse.Summary toSummary(
            Order order,
            Map<String, Member> memberMap,
            Map<Long, Product> productMap,
            Map<Long, String> thumbnailMap) {
        Member buyer = memberMap.get(order.getBuyerId());
        Member seller = memberMap.get(order.getSellerId());
        Product product = productMap.get(order.getProductId());

        return new AdminOrderResponse.Summary(
                order.getOrderId(),
                order.getBuyerId(),
                buyer != null ? buyer.getUserid() : null,
                buyer != null ? buyer.getNickname() : null,
                order.getSellerId(),
                seller != null ? seller.getUserid() : null,
                seller != null ? seller.getNickname() : null,
                order.getProductId(),
                product != null ? product.getTitle() : null,
                thumbnailMap.get(order.getProductId()),
                order.getOrderStatus(),
                order.getProductAmount(),
                order.getFinalPrice(),
                order.getPaidAt(),
                completedAt(order),
                order.getUpdatedAt()
        );
    }

    private Map<String, Member> buildMemberMap(List<Order> orders) {
        List<String> memberIds = orders.stream()
                .flatMap(order -> Stream.of(order.getBuyerId(), order.getSellerId()))
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
    }

    private Map<Long, Product> buildProductMap(List<Order> orders) {
        List<Long> productIds = orders.stream()
                .map(Order::getProductId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
    }

    private Map<Long, String> buildThumbnailMap(Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productImageRepository.findThumbnailsByProductIds(productIds.stream().toList())
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getProduct().getProductId(),
                        ProductImage::getImageUrl,
                        (existing, replacement) -> existing
                ));
    }

    private LocalDateTime completedAt(Order order) {
        if ("DELIVERED".equals(order.getOrderStatus())) {
            return order.getDeliveredAt();
        }
        if ("CANCELLED".equals(order.getOrderStatus())) {
            return order.getCancelledAt();
        }
        return null;
    }

    private String parseOrderStatus(String orderStatus) {
        String value = blankToNull(orderStatus);
        if (value == null) {
            return null;
        }
        try {
            return OrderStatus.valueOf(value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}

package com.nailed.web.order.service;
import com.nailed.web.member.service.SellerGradeService;
import com.nailed.web.order.dto.OrderResponseDto;
import com.nailed.web.order.entity.Order;
import com.nailed.web.order.repository.OrderRepository;
import com.nailed.web.product.entity.Product;
import com.nailed.web.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class MockShippingService implements ShippingService {
    private static final Set<String> ALLOWED_CARRIERS = Set.of("CJ","LOGEN", "HANJIN", "KOREA_POST", "LOTTE");
    private static final Pattern TRACKING_PATTERN = Pattern.compile("^[0-9]{10,13}$");
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerGradeService sellerGradeService;
    @Override
    public OrderResponseDto registerTracking(String orderId, String carrierCode, String trackingNumber) {
        if (!ALLOWED_CARRIERS.contains(carrierCode)) {
            throw new IllegalArgumentException("지원하지 않는 택배사입니다. 허용: " + ALLOWED_CARRIERS);
        }
        if (!TRACKING_PATTERN.matcher(trackingNumber).matches()) {
            throw new IllegalArgumentException("유효하지 않은 운송장 번호입니다. 숫자 10~13자리로 입력해주세요.");
        }
        Order order = findOrder(orderId);
        if (!"REQUESTED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("주문접수 상태의 주문만 운송장을 등록할 수 있습니다.");
        }
        order.startShipping(carrierCode, trackingNumber);
        Order savedOrder = orderRepository.save(order);
        
        sellerGradeService.refreshSellerGrade(savedOrder.getSellerId());

        
        Product product = productRepository.findById(savedOrder.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다. productId=" + savedOrder.getProductId()));
        return OrderResponseDto.from(savedOrder, product.getShippingFee(), product.getPrice());
    }
    @Override
    public OrderResponseDto confirmDelivery(String orderId) {
        Order order = findOrder(orderId);
        if (!"SHIPPING".equals(order.getOrderStatus())) {
            throw new IllegalStateException("배송 중 상태의 주문만 배송 완료 처리할 수 있습니다.");
        }
        order.markAsDelivered();
        Order savedOrder = orderRepository.save(order);
        sellerGradeService.refreshSellerGrade(savedOrder.getSellerId());
        Product product = productRepository.findById(savedOrder.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다. productId=" + savedOrder.getProductId()));
        return OrderResponseDto.from(savedOrder, product.getShippingFee(), product.getPrice());
    }
    private Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId=" + orderId));
    }
}
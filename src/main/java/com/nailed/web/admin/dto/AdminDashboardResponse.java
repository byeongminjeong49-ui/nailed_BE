package com.nailed.web.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardResponse {

    public record Dashboard(
            MemberStats members,
            ProductStats products,
            OrderStats orders,
            SalesStats sales,
            ReportStats reports,
            InquiryStats inquiries,
            List<RecentOrder> recentOrders,
            List<RecentReport> recentReports,
            List<RecentProduct> recentProducts,
            List<RecentMember> recentMembers
    ) {}

    public record MemberStats(
            long totalMembers,
            long userMembers,
            long adminMembers,
            long activeMembers,
            long lockedMembers,
            long withdrawnMembers,
            long suspendedMembers,
            long bannedMembers
    ) {}

    public record ProductStats(
            long totalProducts,
            long onSaleProducts,
            long soldProducts,
            long deletedProducts
    ) {}

    public record OrderStats(
            long totalOrders,
            long requestedOrders,
            long paidOrders,
            long shippingOrders,
            long deliveredOrders,
            long cancelledOrders
    ) {}

    public record SalesStats(
            long deliveredOrderCount,
            long commissionRevenue,
            long transactionAmount
    ) {}

    public record ReportStats(
            long totalReports,
            long approvedReports,   // 처리 대기 (APPROVED = 접수됨)
            long rejectedReports,
            long doneReports
    ) {}

    public record InquiryStats(
            long totalInquiries,
            long pendingInquiries,
            long answeredInquiries
    ) {}

    public record RecentOrder(
            String orderId,
            String productTitle,
            String buyerNickname,
            String sellerNickname,
            String orderStatus,
            Integer finalPrice,
            LocalDateTime paidAt
    ) {}

    public record RecentReport(
            String reportId,
            String reporterNickname,
            String targetName,
            String reasonCode,
            String status,
            LocalDateTime createdAt
    ) {}

    public record RecentProduct(
            Long productId,
            String title,
            String productStatus,
            Integer price,
            String thumbnailUrl,
            LocalDateTime createdAt
    ) {}

    public record RecentMember(
            String memberId,
            String userid,
            String nickname,
            String role,
            String status,
            LocalDateTime createdAt
    ) {}
}

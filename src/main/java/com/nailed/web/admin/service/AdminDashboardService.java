package com.nailed.web.admin.service;

import com.nailed.web.admin.dto.AdminDashboardResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    public AdminDashboardResponse.Dashboard getDashboard() {
        return new AdminDashboardResponse.Dashboard(
                memberStats(),
                productStats(),
                orderStats(),
                salesStats(),
                reportStats(),
                inquiryStats(),
                recentOrders(),
                recentReports(),
                recentProducts(),
                recentMembers()
        );
    }

    private AdminDashboardResponse.MemberStats memberStats() {
        return new AdminDashboardResponse.MemberStats(
                count("SELECT COUNT(*) FROM members"),
                count("SELECT COUNT(*) FROM members WHERE role = 'USER'"),
                count("SELECT COUNT(*) FROM members WHERE role = 'ADMIN'"),
                count("SELECT COUNT(*) FROM members WHERE member_status = 'ACTIVE'"),
                count("SELECT COUNT(*) FROM members WHERE member_status = 'LOCKED'"),
                count("SELECT COUNT(*) FROM members WHERE member_status IN ('WITHDRAWN', 'WITHDRAW')"),
                count("SELECT COUNT(*) FROM members WHERE member_status IN ('SUSPEND', 'SUSPENDED')"),
                count("SELECT COUNT(*) FROM members WHERE member_status = 'BANNED'")
        );
    }

    private AdminDashboardResponse.ProductStats productStats() {
        return new AdminDashboardResponse.ProductStats(
                count("SELECT COUNT(*) FROM products"),
                count("SELECT COUNT(*) FROM products WHERE product_status = 'ON_SALE'"),
                count("SELECT COUNT(*) FROM products WHERE product_status = 'SOLD'"),
                count("SELECT COUNT(*) FROM products WHERE product_status = 'DELETED'")
        );
    }

    private AdminDashboardResponse.OrderStats orderStats() {
        return new AdminDashboardResponse.OrderStats(
                count("SELECT COUNT(*) FROM orders"),
                count("SELECT COUNT(*) FROM orders WHERE order_status = 'REQUESTED'"),
                count("SELECT COUNT(*) FROM orders WHERE order_status = 'PAID'"),
                count("SELECT COUNT(*) FROM orders WHERE order_status = 'SHIPPING'"),
                count("SELECT COUNT(*) FROM orders WHERE order_status = 'DELIVERED'"),
                count("SELECT COUNT(*) FROM orders WHERE order_status = 'CANCELLED'")
        );
    }

    private AdminDashboardResponse.SalesStats salesStats() {
        Object[] row = singleRow("""
                SELECT COUNT(*),
                       COALESCE(SUM(ROUND((final_price * commission / 100), -1)), 0),
                       COALESCE(SUM(final_price), 0)
                FROM orders
                WHERE order_status IN ('REQUESTED', 'SHIPPING', 'DELIVERED')
                """);
        return new AdminDashboardResponse.SalesStats(
                number(row[0]).longValue(),
                number(row[1]).longValue(),
                number(row[2]).longValue()
        );
    }

    private AdminDashboardResponse.ReportStats reportStats() {
        return new AdminDashboardResponse.ReportStats(
                count("SELECT COUNT(*) FROM reports"),
                count("SELECT COUNT(*) FROM reports WHERE report_status = 'PENDING'"),
                count("SELECT COUNT(*) FROM reports WHERE report_status = 'APPROVED'"),
                count("SELECT COUNT(*) FROM reports WHERE report_status = 'REJECTED'"),
                count("SELECT COUNT(*) FROM reports WHERE report_status = 'DONE'")
        );
    }

    private AdminDashboardResponse.InquiryStats inquiryStats() {
        return new AdminDashboardResponse.InquiryStats(
                count("SELECT COUNT(*) FROM inquiries"),
                count("SELECT COUNT(*) FROM inquiries WHERE inquiry_status = 'PENDING'"),
                count("SELECT COUNT(*) FROM inquiries WHERE inquiry_status = 'ANSWERED'")
        );
    }

    private List<AdminDashboardResponse.RecentOrder> recentOrders() {
        return rows("""
                SELECT o.order_id, p.title, buyer.nickname, seller.nickname,
                       o.order_status, o.final_price, o.paid_at
                FROM orders o
                LEFT JOIN products p ON p.product_id = o.product_id
                LEFT JOIN members buyer ON buyer.member_id = o.buyer_id
                LEFT JOIN members seller ON seller.member_id = o.seller_id
                ORDER BY o.paid_at DESC
                LIMIT 5
                """).stream()
                .map(row -> new AdminDashboardResponse.RecentOrder(
                        string(row[0]),
                        string(row[1]),
                        string(row[2]),
                        string(row[3]),
                        string(row[4]),
                        integer(row[5]),
                        time(row[6])
                ))
                .toList();
    }

    private List<AdminDashboardResponse.RecentReport> recentReports() {
        return rows("""
                SELECT r.report_id, reporter.nickname, target.nickname,
                       r.reason_code, r.report_status, r.created_at
                FROM reports r
                LEFT JOIN members reporter ON reporter.member_id = r.reporter_id
                LEFT JOIN members target ON target.member_id = r.target_member_id
                ORDER BY r.created_at DESC
                LIMIT 5
                """).stream()
                .map(row -> new AdminDashboardResponse.RecentReport(
                        string(row[0]),
                        string(row[1]),
                        string(row[2]),
                        string(row[3]),
                        string(row[4]),
                        time(row[5])
                ))
                .toList();
    }

    private List<AdminDashboardResponse.RecentProduct> recentProducts() {
        return rows("""
                SELECT p.product_id, p.title, p.product_status, p.price,
                       pi.image_url, p.created_at
                FROM products p
                LEFT JOIN product_images pi
                    ON pi.product_id = p.product_id AND pi.sort_order = 0
                ORDER BY p.created_at DESC
                LIMIT 5
                """).stream()
                .map(row -> new AdminDashboardResponse.RecentProduct(
                        number(row[0]).longValue(),
                        string(row[1]),
                        string(row[2]),
                        integer(row[3]),
                        string(row[4]),
                        time(row[5])
                ))
                .toList();
    }

    private List<AdminDashboardResponse.RecentMember> recentMembers() {
        return rows("""
                SELECT member_id, userid, nickname, role, member_status, created_at
                FROM members
                ORDER BY created_at DESC
                LIMIT 5
                """).stream()
                .map(row -> new AdminDashboardResponse.RecentMember(
                        string(row[0]),
                        string(row[1]),
                        string(row[2]),
                        string(row[3]),
                        string(row[4]),
                        time(row[5])
                ))
                .toList();
    }

    private long count(String sql) {
        return number(entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    private Object[] singleRow(String sql) {
        return (Object[]) entityManager.createNativeQuery(sql).getSingleResult();
    }

    private List<Object[]> rows(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        List<?> raw = query.getResultList();
        List<Object[]> rows = new ArrayList<>();
        for (Object row : raw) {
            rows.add((Object[]) row);
        }
        return rows;
    }

    private Number number(Object value) {
        return (Number) value;
    }

    private Integer integer(Object value) {
        return value != null ? number(value).intValue() : null;
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
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
}

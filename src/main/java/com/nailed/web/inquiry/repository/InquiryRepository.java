package com.nailed.web.inquiry.repository;

import com.nailed.common.enums.InquiryStatus;
import com.nailed.web.inquiry.entity.Inquiry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

    Page<Inquiry> findByMemberMemberId(String memberId, Pageable pageable);

    Optional<Inquiry> findByInquiryIdAndMemberMemberId(String inquiryId, String memberId);

    Page<Inquiry> findByInquiryStatus(InquiryStatus inquiryStatus, Pageable pageable);

    @Query(value = """
            SELECT DATE_FORMAT(created_at, :dateFormat) AS label, COUNT(*) AS count
            FROM inquiries
            WHERE created_at >= :startAt
              AND created_at < :endAt
            GROUP BY DATE_FORMAT(created_at, :dateFormat)
            """, nativeQuery = true)
    List<Object[]> countInquiriesByPeriod(
            @Param("dateFormat") String dateFormat,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}

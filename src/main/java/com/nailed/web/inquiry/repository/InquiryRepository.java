package com.nailed.web.inquiry.repository;

import com.nailed.common.enums.InquiryStatus;
import com.nailed.web.inquiry.entity.Inquiry;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

    Page<Inquiry> findByMemberMemberId(String memberId, Pageable pageable);

    Optional<Inquiry> findByInquiryIdAndMemberMemberId(String inquiryId, String memberId);

    Page<Inquiry> findByInquiryStatus(InquiryStatus inquiryStatus, Pageable pageable);
}

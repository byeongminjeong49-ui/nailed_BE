package com.nailed.domain.report.service;

import com.nailed.common.exception.BusinessException;
import com.nailed.common.exception.ErrorCode;
import com.nailed.domain.member.entity.Member;
import com.nailed.domain.member.repository.MemberRepository;
import com.nailed.domain.product.entity.Product;
import com.nailed.domain.product.repository.ProductRepository;
import com.nailed.domain.report.dto.ReportRequest;
import com.nailed.domain.report.dto.ReportResponse;
import com.nailed.domain.report.entity.Report;
import com.nailed.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final AtomicLong sequence = new AtomicLong(1);

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public String submit(String reporterId, ReportRequest.Submit request) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Member targetMember = null;
        Product targetProduct = null;

        if ("MEMBER".equals(request.targetType())) {
            targetMember = memberRepository.findById(request.targetMemberId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        } else if ("PRODUCT".equals(request.targetType())) {
            targetProduct = productRepository.findById(request.targetProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        }

        String reportId = "RPT_" + String.format("%03d", sequence.getAndIncrement());

        Report report = Report.builder()
                .reportId(reportId)
                .reporter(reporter)
                .targetType(request.targetType())
                .reasonCode(request.reasonCode())
                .detail(request.detail())
                .targetMember(targetMember)
                .targetProduct(targetProduct)
                .build();

        return reportRepository.save(report).getReportId();
    }

    public Page<ReportResponse.Detail> getPendingReports(Pageable pageable) {
        return reportRepository.findByStatus("PENDING", pageable)
                .map(ReportResponse.Detail::from);
    }

    @Transactional
    public void process(String reportId, ReportRequest.Process request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        switch (request.action()) {
            case "APPROVE" -> report.approve(request.reason());
            case "REJECT"  -> report.reject(request.reason());
            case "DONE"    -> report.done(request.reason());
            default        -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

//package com.nailed.common.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.EntityListeners;
//import jakarta.persistence.MappedSuperclass;
//import lombok.Getter;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//
///**
// * Soft Delete 불필요한 Entity의 베이스 클래스
// * 대상: Order, Payment, Delivery, Report 등 상태값으로만 관리하는 Entity
// *
// * 사용법: public class Order extends BaseEntity { ... }
// */
//@Getter
//@MappedSuperclass
//@EntityListeners(AuditingEntityListener.class)
//public abstract class BaseEntity {
//
//    @CreatedDate
//    @Column(updatable = false, nullable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;
//}

//package com.nailed.web.member.repository;
//
//import com.nailed.web.member.entity.MemberProfileImage;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface MemberProfileImageRepository extends JpaRepository<MemberProfileImage, Long> {
//
//    @Modifying
//    @Query("""
//            UPDATE MemberProfileImage mpi
//            SET mpi.current = false
//            WHERE mpi.member.memberId = :memberId
//              AND mpi.current = true
//            """)
//    int clearCurrentByMemberId(@Param("memberId") String memberId);
//}

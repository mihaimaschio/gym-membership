package com.gym_membership.repository;

import com.gym_membership.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {
    List<ClassEnrollment> findByMemberId(Long memberId);
    
    List<ClassEnrollment> findByGymClassId(Long gymClassId);
    
    @Query("SELECT COUNT(e) FROM ClassEnrollment e WHERE e.gymClass.id = :classId")
    long countEnrollmentsByClassId(Long classId);
    
    boolean existsByMemberIdAndGymClassId(Long memberId, Long gymClassId);
}

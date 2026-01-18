package com.gym_membership.repository;

import com.gym_membership.entity.Member;
import com.gym_membership.enums.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    
    @Query("SELECT m FROM Member m WHERE m.subscription.type = :type")
    List<Member> findBySubscriptionType(SubscriptionType type);
    
    @Query("SELECT m FROM Member m WHERE m.subscription.endDate >= CURRENT_DATE")
    List<Member> findMembersWithActiveSubscription();
}

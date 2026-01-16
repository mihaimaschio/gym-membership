package com.gym_membership.repository;

import com.gym_membership.entity.Subscription;
import com.gym_membership.enums.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByMemberId(Long memberId);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate >= CURRENT_DATE")
    List<Subscription> findActiveSubscriptions();
    
    List<Subscription> findByType(SubscriptionType type);
}

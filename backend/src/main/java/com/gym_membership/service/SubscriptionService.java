package com.gym_membership.service;

import com.gym_membership.entity.Member;
import com.gym_membership.entity.Subscription;
import com.gym_membership.enums.SubscriptionType;
import com.gym_membership.repository.MemberRepository;
import com.gym_membership.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + id));
    }

    public Subscription getSubscriptionByMemberId(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Subscription not found for member id: " + memberId));
    }

    @Transactional
    public Subscription createSubscription(Long memberId, SubscriptionType type) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        Subscription subscription = member.getSubscription();
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setMember(member);
        }

        subscription.setType(type);
        subscription.setPrice(type.getPrice());
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(calculateEndDate(type));

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription renewSubscription(Long subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(calculateEndDate(subscription.getType()));
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        System.out.println("Deleting subscription with id: " + id);
        try {
            Subscription subscription = subscriptionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            
            Member member = subscription.getMember();
            if (member != null) {
                member.setSubscription(null);
                memberRepository.save(member);
                System.out.println("Removed subscription from member, orphan removal should handle deletion");
            } else {
                // Fallback if no member attached (shouldn't happen given the schema)
                subscriptionRepository.delete(subscription);
            }
        } catch (Exception e) {
            System.err.println("Error deleting subscription: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Subscription> getActiveSubscriptions() {
        return subscriptionRepository.findActiveSubscriptions();
    }

    public List<Subscription> getSubscriptionsByType(SubscriptionType type) {
        return subscriptionRepository.findByType(type);
    }

    private LocalDate calculateEndDate(SubscriptionType type) {
        LocalDate startDate = LocalDate.now();
        return switch (type) {
            case ONE_DAY -> startDate.plusDays(1);
            case FULL_MONTHLY, STUDENT_MONTHLY -> startDate.plusMonths(1);
            case SIX_MONTHS -> startDate.plusMonths(6);
            case ONE_YEAR -> startDate.plusYears(1);
        };
    }
}

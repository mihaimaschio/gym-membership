package com.gym_membership.service;

import com.gym_membership.entity.Member;
import com.gym_membership.entity.Subscription;
import com.gym_membership.enums.SubscriptionType;
import com.gym_membership.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found with email: " + email));
    }

    @Transactional
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Long id, Member memberDetails) {
        Member member = getMemberById(id);
        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setPhoneNumber(memberDetails.getPhoneNumber());
        member.setEmail(memberDetails.getEmail());
        member.setProfilePicture(memberDetails.getProfilePicture());
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public List<Member> getMembersBySubscriptionType(SubscriptionType type) {
        return memberRepository.findBySubscriptionType(type);
    }

    public List<Member> getMembersWithActiveSubscription() {
        return memberRepository.findMembersWithActiveSubscription();
    }

    public boolean hasActiveSubscription(Long memberId) {
        Member member = getMemberById(memberId);
        Subscription subscription = member.getSubscription();
        return subscription != null && subscription.isActive();
    }
}

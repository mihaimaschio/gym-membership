package com.gym_membership.controller;

import com.gym_membership.entity.Member;
import com.gym_membership.enums.SubscriptionType;
import com.gym_membership.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) {
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member member) {
        return ResponseEntity.ok(memberService.updateMember(id, member));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscription-type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Member>> getMembersBySubscriptionType(@PathVariable SubscriptionType type) {
        return ResponseEntity.ok(memberService.getMembersBySubscriptionType(type));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Member>> getMembersWithActiveSubscription() {
        return ResponseEntity.ok(memberService.getMembersWithActiveSubscription());
    }

    @GetMapping("/{id}/has-active-subscription")
    public ResponseEntity<Boolean> hasActiveSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.hasActiveSubscription(id));
    }
}

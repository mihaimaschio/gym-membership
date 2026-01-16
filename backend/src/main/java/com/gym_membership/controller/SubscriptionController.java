package com.gym_membership.controller;

import com.gym_membership.entity.Subscription;
import com.gym_membership.enums.SubscriptionType;
import com.gym_membership.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<Subscription> getSubscriptionByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByMemberId(memberId));
    }

    @PostMapping("/member/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subscription> createSubscription(
            @PathVariable Long memberId,
            @RequestParam SubscriptionType type) {
        return ResponseEntity.ok(subscriptionService.createSubscription(memberId, type));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<Subscription> renewSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubscription(@PathVariable Long id) {
        System.out.println("Received request to delete subscription: " + id);
        try {
            subscriptionService.deleteSubscription(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Controller error deleting subscription: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error deleting subscription: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Subscription>> getActiveSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Subscription>> getSubscriptionsByType(@PathVariable SubscriptionType type) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByType(type));
    }
}

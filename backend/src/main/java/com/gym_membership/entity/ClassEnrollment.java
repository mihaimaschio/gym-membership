package com.gym_membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Member member;

    @ManyToOne
    @JoinColumn(name = "gym_class_id", nullable = false)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private GymClass gymClass;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    @PrePersist
    protected void onCreate() {
        enrollmentDate = LocalDateTime.now();
    }
}

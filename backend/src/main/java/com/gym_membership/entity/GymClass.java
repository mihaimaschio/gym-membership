package com.gym_membership.entity;

import com.gym_membership.enums.ClassType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gym_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private int maxCapacity = 20;

    @OneToMany(mappedBy = "gymClass", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<ClassEnrollment> enrollments = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonProperty("enrollmentCount")
    public int getEnrollmentCount() {
        System.out.println("Getting enrollment count for class " + id + ": " + enrollments.size());
        return enrollments.size();
    }

    public int getAvailableSpots() {
        return maxCapacity - enrollments.size();
    }

    public boolean hasAvailableSpots() {
        return getAvailableSpots() > 0;
    }
}

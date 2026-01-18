package com.gym_membership.controller;

import com.gym_membership.entity.ClassEnrollment;
import com.gym_membership.entity.GymClass;
import com.gym_membership.enums.ClassType;
import com.gym_membership.service.GymClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {
    private final GymClassService gymClassService;

    @GetMapping
    public ResponseEntity<List<GymClass>> getAllClasses() {
        return ResponseEntity.ok(gymClassService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymClass> getClassById(@PathVariable Long id) {
        return ResponseEntity.ok(gymClassService.getClassById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GymClass> createClass(
            @RequestParam ClassType type,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam LocalTime time) {
        return ResponseEntity.ok(gymClassService.createClass(type, dayOfWeek, time));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GymClass> updateClass(@PathVariable Long id, @RequestBody GymClass gymClass) {
        return ResponseEntity.ok(gymClassService.updateClass(id, gymClass));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        gymClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<GymClass>> getClassesByDay(@PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(gymClassService.getClassesByDay(dayOfWeek));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<GymClass>> getClassesByType(@PathVariable ClassType type) {
        return ResponseEntity.ok(gymClassService.getClassesByType(type));
    }

    @GetMapping("/available")
    public ResponseEntity<List<GymClass>> getClassesWithAvailableSpots() {
        return ResponseEntity.ok(gymClassService.getClassesWithAvailableSpots());
    }

    @GetMapping("/available/day/{dayOfWeek}")
    public ResponseEntity<List<GymClass>> getAvailableClassesByDay(@PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(gymClassService.getAvailableClassesByDay(dayOfWeek));
    }

    @PostMapping("/{classId}/enroll/{memberId}")
    public ResponseEntity<ClassEnrollment> enrollMemberInClass(
            @PathVariable Long classId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(gymClassService.enrollMemberInClass(memberId, classId));
    }

    @DeleteMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<Void> cancelEnrollment(@PathVariable Long enrollmentId) {
        gymClassService.cancelEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member/{memberId}/enrollments")
    public ResponseEntity<List<ClassEnrollment>> getMemberEnrollments(@PathVariable Long memberId) {
        return ResponseEntity.ok(gymClassService.getMemberEnrollments(memberId));
    }

    @GetMapping("/{classId}/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClassEnrollment>> getClassEnrollments(@PathVariable Long classId) {
        return ResponseEntity.ok(gymClassService.getClassEnrollments(classId));
    }
}

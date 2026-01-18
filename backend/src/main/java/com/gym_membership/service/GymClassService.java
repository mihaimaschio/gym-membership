package com.gym_membership.service;

import com.gym_membership.entity.ClassEnrollment;
import com.gym_membership.entity.GymClass;
import com.gym_membership.entity.Member;
import com.gym_membership.enums.ClassType;
import com.gym_membership.repository.ClassEnrollmentRepository;
import com.gym_membership.repository.GymClassRepository;
import com.gym_membership.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GymClassService {
    private final GymClassRepository gymClassRepository;
    private final ClassEnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;

    public List<GymClass> getAllClasses() {
        return gymClassRepository.findAll();
    }

    public GymClass getClassById(Long id) {
        return gymClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
    }

    @Transactional
    public GymClass createClass(ClassType type, DayOfWeek dayOfWeek, LocalTime time) {
        GymClass gymClass = new GymClass();
        gymClass.setType(type);
        gymClass.setDayOfWeek(dayOfWeek);
        gymClass.setTime(time);
        gymClass.setMaxCapacity(20);
        return gymClassRepository.save(gymClass);
    }

    @Transactional
    public GymClass updateClass(Long id, GymClass classDetails) {
        GymClass gymClass = getClassById(id);
        gymClass.setType(classDetails.getType());
        gymClass.setDayOfWeek(classDetails.getDayOfWeek());
        gymClass.setTime(classDetails.getTime());
        return gymClassRepository.save(gymClass);
    }

    @Transactional
    public void deleteClass(Long id) {
        gymClassRepository.deleteById(id);
    }

    public List<GymClass> getClassesByDay(DayOfWeek dayOfWeek) {
        return gymClassRepository.findByDayOfWeek(dayOfWeek);
    }

    public List<GymClass> getClassesByType(ClassType type) {
        return gymClassRepository.findByType(type);
    }

    public List<GymClass> getClassesWithAvailableSpots() {
        return gymClassRepository.findClassesWithAvailableSpots();
    }

    public List<GymClass> getAvailableClassesByDay(DayOfWeek dayOfWeek) {
        return gymClassRepository.findAvailableClassesByDay(dayOfWeek);
    }

    @Transactional
    public ClassEnrollment enrollMemberInClass(Long memberId, Long classId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
        
        GymClass gymClass = getClassById(classId);

        // Check if member has active subscription
        if (member.getSubscription() == null || !member.getSubscription().isActive()) {
            throw new RuntimeException("Member does not have an active subscription");
        }

        // Check if class has available spots
        if (!gymClass.hasAvailableSpots()) {
            throw new RuntimeException("Class is full");
        }

        // Check if member is already enrolled
        if (enrollmentRepository.existsByMemberIdAndGymClassId(memberId, classId)) {
            throw new RuntimeException("Member is already enrolled in this class");
        }

        ClassEnrollment enrollment = new ClassEnrollment();
        enrollment.setMember(member);
        enrollment.setGymClass(gymClass);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        ClassEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        System.out.println("Cancelling enrollment: " + enrollmentId);

        if (enrollment.getMember() != null) {
            enrollment.getMember().getEnrollments().remove(enrollment);
        }

        if (enrollment.getGymClass() != null) {
            enrollment.getGymClass().getEnrollments().remove(enrollment);
        }

        enrollmentRepository.delete(enrollment);
        System.out.println("Enrollment deleted: " + enrollmentId);
    }

    public List<ClassEnrollment> getMemberEnrollments(Long memberId) {
        return enrollmentRepository.findByMemberId(memberId);
    }

    public List<ClassEnrollment> getClassEnrollments(Long classId) {
        return enrollmentRepository.findByGymClassId(classId);
    }
}

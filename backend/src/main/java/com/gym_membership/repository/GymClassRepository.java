package com.gym_membership.repository;

import com.gym_membership.entity.GymClass;
import com.gym_membership.enums.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {
    List<GymClass> findByDayOfWeek(DayOfWeek dayOfWeek);
    
    List<GymClass> findByType(ClassType type);
    
    @Query("SELECT g FROM GymClass g WHERE SIZE(g.enrollments) < g.maxCapacity")
    List<GymClass> findClassesWithAvailableSpots();
    
    @Query("SELECT g FROM GymClass g WHERE g.dayOfWeek = :dayOfWeek AND SIZE(g.enrollments) < g.maxCapacity")
    List<GymClass> findAvailableClassesByDay(DayOfWeek dayOfWeek);
}

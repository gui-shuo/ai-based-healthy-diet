package com.nutriai.repository;

import com.nutriai.entity.MealPlanCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanCheckinRepository extends JpaRepository<MealPlanCheckin, Long> {

    List<MealPlanCheckin> findByFollowId(Long followId);

    List<MealPlanCheckin> findByFollowIdAndDayNumber(Long followId, Integer dayNumber);

    Optional<MealPlanCheckin> findByFollowIdAndDayNumberAndMealType(Long followId, Integer dayNumber, String mealType);

    @Query("SELECT COUNT(DISTINCT c.dayNumber) FROM MealPlanCheckin c WHERE c.followId = :followId")
    int countCheckedDays(@Param("followId") Long followId);

    @Query("SELECT COUNT(c) FROM MealPlanCheckin c WHERE c.followId = :followId")
    int countCheckins(@Param("followId") Long followId);
}

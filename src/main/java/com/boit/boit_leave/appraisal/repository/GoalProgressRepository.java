package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalProgressRepository extends JpaRepository<GoalProgress, Long> {

    List<GoalProgress> findByGoalIdOrderByUpdatedAtDesc(
            Long goalId
    );

    Optional<GoalProgress> findFirstByGoalIdOrderByUpdatedAtDesc(
            Long goalId
    );
}
package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.AppraisalGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AppraisalGoalRepository extends JpaRepository<AppraisalGoal, Long> {

    List<AppraisalGoal> findByAppraisalIdOrderByIdAsc(
            Long appraisalId
    );

    Optional<AppraisalGoal> findByIdAndAppraisalId(
            Long id,
            Long appraisalId
    );

    long countByAppraisalId(
            Long appraisalId
    );

    @Query("""
        SELECT COALESCE(SUM(g.weight), 0)
        FROM AppraisalGoal g
        WHERE g.appraisal.id = :appraisalId
    """)
    BigDecimal sumWeightByAppraisalId(
            @Param("appraisalId") Long appraisalId
    );
}
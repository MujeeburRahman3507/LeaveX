package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.AppraisalCycle;
import com.boit.boit_leave.appraisal.enums.AppraisalCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppraisalCycleRepository extends JpaRepository<AppraisalCycle, Long> {

    List<AppraisalCycle> findAllByOrderByAppraisalStartDateDesc();

    Optional<AppraisalCycle> findByName(String name);

    Optional<AppraisalCycle> findFirstByStatusOrderByAppraisalStartDateDesc(
            AppraisalCycleStatus status
    );
}
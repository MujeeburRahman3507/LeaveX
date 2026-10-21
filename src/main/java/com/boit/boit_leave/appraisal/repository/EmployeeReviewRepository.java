package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.EmployeeReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeReviewRepository extends JpaRepository<EmployeeReview, Long> {

    Optional<EmployeeReview> findByAppraisalId(
            Long appraisalId
    );
}
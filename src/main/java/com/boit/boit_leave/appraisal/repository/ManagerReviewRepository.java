package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.ManagerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerReviewRepository extends JpaRepository<ManagerReview, Long> {

    Optional<ManagerReview> findByAppraisalId(
            Long appraisalId
    );
}
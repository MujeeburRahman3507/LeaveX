package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.HrReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HrReviewRepository extends JpaRepository<HrReview, Long> {

    Optional<HrReview> findByAppraisalId(
            Long appraisalId
    );
}
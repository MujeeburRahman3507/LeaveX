package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.ManagerReviewRequest;
import com.boit.boit_leave.appraisal.dto.ManagerReviewResponse;

public interface ManagerReviewService {

    ManagerReviewResponse saveReview(
            ManagerReviewRequest request,
            Long managerId
    );

    ManagerReviewResponse getReview(
            Long appraisalId
    );

    ManagerReviewResponse submitReview(
            Long appraisalId,
            Long managerId
    );
    
    void startReview(Long appraisalId, Long managerId);
}
package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.HrReviewRequest;
import com.boit.boit_leave.appraisal.dto.HrReviewResponse;

public interface HrReviewService {

    HrReviewResponse saveReview(
            HrReviewRequest request,
            Long adminId
    );

    HrReviewResponse getReview(
            Long appraisalId
    );

    HrReviewResponse submitReview(
            Long appraisalId,
            Long adminId
    );

    HrReviewResponse finalizeReview(
            Long appraisalId,
            Long adminId
    );
    
    void startReview(Long appraisalId, Long adminId);
}
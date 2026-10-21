package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.EmployeeReviewRequest;
import com.boit.boit_leave.appraisal.dto.EmployeeReviewResponse;

public interface EmployeeReviewService {

    EmployeeReviewResponse saveReview(
            EmployeeReviewRequest request,
            Long employeeId
    );

    EmployeeReviewResponse getReview(
            Long appraisalId
    );

    EmployeeReviewResponse submitReview(
            Long appraisalId,
            Long employeeId
    );
}
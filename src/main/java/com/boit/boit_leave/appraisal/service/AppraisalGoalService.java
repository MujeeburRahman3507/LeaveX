package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.AppraisalGoalRequest;
import com.boit.boit_leave.appraisal.dto.AppraisalGoalResponse;

import java.util.List;

public interface AppraisalGoalService {

    AppraisalGoalResponse createGoal(
            AppraisalGoalRequest request,
            Long employeeId
    );

    AppraisalGoalResponse updateGoal(
            Long goalId,
            AppraisalGoalRequest request,
            Long employeeId
    );

    AppraisalGoalResponse getGoalById(
            Long goalId
    );

    List<AppraisalGoalResponse> getGoalsByAppraisal(
            Long appraisalId
    );

    void deleteGoal(
            Long goalId,
            Long employeeId
    );
}
package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.GoalProgressRequest;
import com.boit.boit_leave.appraisal.dto.GoalProgressResponse;

import java.util.List;

public interface GoalProgressService {

    GoalProgressResponse updateProgress(
            Long goalId,
            GoalProgressRequest request,
            Long employeeId
    );

    GoalProgressResponse getLatestProgress(
            Long goalId
    );

    List<GoalProgressResponse> getProgressHistory(
            Long goalId
    );
}
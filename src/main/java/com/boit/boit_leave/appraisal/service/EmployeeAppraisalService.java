package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.EmployeeAppraisalResponse;

import java.util.List;

public interface EmployeeAppraisalService {

    EmployeeAppraisalResponse createAppraisal(
            Long cycleId,
            Long employeeId
    );

    EmployeeAppraisalResponse getById(
            Long appraisalId
    );

    EmployeeAppraisalResponse getEmployeeAppraisal(
            Long cycleId,
            Long employeeId
    );

    List<EmployeeAppraisalResponse> getEmployeeAppraisals(
            Long employeeId
    );

    List<EmployeeAppraisalResponse> getManagerAppraisals(
            Long managerId
    );

    List<EmployeeAppraisalResponse> getAllAppraisals();

    EmployeeAppraisalResponse submitGoals(
            Long appraisalId,
            Long employeeId
    );

    EmployeeAppraisalResponse approveGoals(
            Long appraisalId,
            Long managerId
    );

    EmployeeAppraisalResponse requestGoalRevision(
            Long appraisalId,
            Long managerId
    );
    
    void startSelfReview(Long appraisalId, Long employeeId);
}
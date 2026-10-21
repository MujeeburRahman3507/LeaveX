package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.AppraisalCycleRequest;
import com.boit.boit_leave.appraisal.dto.AppraisalCycleResponse;

import java.util.List;

public interface AppraisalCycleService {

    AppraisalCycleResponse createCycle(
            AppraisalCycleRequest request,
            Long createdBy
    );

    AppraisalCycleResponse updateCycle(
            Long cycleId,
            AppraisalCycleRequest request
    );

    AppraisalCycleResponse getCycleById(
            Long cycleId
    );

    List<AppraisalCycleResponse> getAllCycles();

    AppraisalCycleResponse updateCycleStatus(
            Long cycleId,
            String status
    );
}
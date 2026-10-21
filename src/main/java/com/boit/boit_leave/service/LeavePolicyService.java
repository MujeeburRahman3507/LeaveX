package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.LeavePolicyDto;

import java.util.List;

public interface LeavePolicyService {

    LeavePolicyDto createPolicy(
            LeavePolicyDto dto);

    LeavePolicyDto updatePolicy(
            Long id,
            LeavePolicyDto dto);

    void deletePolicy(
            Long id);

    List<LeavePolicyDto> getAllPolicies();

    LeavePolicyDto getPolicyById(
            Long id);
}
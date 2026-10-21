package com.boit.boit_leave.service.impl;

import com.boit.boit_leave.dto.LeavePolicyDto;
import com.boit.boit_leave.entity.LeavePolicy;
import com.boit.boit_leave.repository.LeavePolicyRepository;
import com.boit.boit_leave.service.LeavePolicyService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeavePolicyServiceImpl
        implements LeavePolicyService {

    private final LeavePolicyRepository
            leavePolicyRepository;

    @Override
    public LeavePolicyDto createPolicy(
            LeavePolicyDto dto) {

        LeavePolicy policy =
                LeavePolicy.builder()
                        .policyName(
                                dto.getPolicyName())
                        .annualLeave(
                                dto.getAnnualLeave())
                        .medicalLeave(
                                dto.getMedicalLeave())
                        .casualLeave(
                                dto.getCasualLeave())
                        .earnedLeave(
                                dto.getEarnedLeave())
                        .createdAt(
                                LocalDateTime.now())
                        .updatedAt(
                                LocalDateTime.now())
                        .build();

        return mapToDto(
                leavePolicyRepository
                        .save(policy));
    }

    @Override
    public LeavePolicyDto updatePolicy(
            Long id,
            LeavePolicyDto dto) {

        LeavePolicy policy =
                leavePolicyRepository
                        .findById(id)
                        .orElseThrow();

        policy.setPolicyName(
                dto.getPolicyName());

        policy.setAnnualLeave(
                dto.getAnnualLeave());

        policy.setMedicalLeave(
                dto.getMedicalLeave());

        policy.setCasualLeave(
                dto.getCasualLeave());

        policy.setEarnedLeave(
                dto.getEarnedLeave());

        policy.setUpdatedAt(
                LocalDateTime.now());

        return mapToDto(
                leavePolicyRepository
                        .save(policy));
    }

    @Override
    public void deletePolicy(
            Long id) {

        leavePolicyRepository
                .deleteById(id);
    }

    @Override
    public List<LeavePolicyDto>
    getAllPolicies() {

        return leavePolicyRepository
                .findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public LeavePolicyDto getPolicyById(
            Long id) {

        return leavePolicyRepository
                .findById(id)
                .map(this::mapToDto)
                .orElseThrow();
    }

    private LeavePolicyDto mapToDto(
            LeavePolicy policy) {

        return LeavePolicyDto.builder()
                .id(policy.getId())
                .policyName(
                        policy.getPolicyName())
                .annualLeave(
                        policy.getAnnualLeave())
                .medicalLeave(
                        policy.getMedicalLeave())
                .casualLeave(
                        policy.getCasualLeave())
                .earnedLeave(
                        policy.getEarnedLeave())
                .build();
    }
}
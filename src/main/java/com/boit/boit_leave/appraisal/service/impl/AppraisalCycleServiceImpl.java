package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.AppraisalCycleRequest;
import com.boit.boit_leave.appraisal.dto.AppraisalCycleResponse;
import com.boit.boit_leave.appraisal.entity.AppraisalCycle;
import com.boit.boit_leave.appraisal.enums.AppraisalCycleStatus;
import com.boit.boit_leave.appraisal.repository.AppraisalCycleRepository;
import com.boit.boit_leave.appraisal.service.AppraisalCycleService;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.entity.Role;
import com.boit.boit_leave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppraisalCycleServiceImpl implements AppraisalCycleService {

    private final AppraisalCycleRepository appraisalCycleRepository;
    private final EmployeeAppraisalRepository employeeAppraisalRepository;
    private final UserRepository userRepository;
    
    @Override
    public AppraisalCycleResponse createCycle(
            AppraisalCycleRequest request,
            Long createdBy
    ) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Appraisal cycle name is required");
        }

        if (request.getAppraisalStartDate() == null ||
                request.getAppraisalEndDate() == null) {
            throw new IllegalArgumentException(
                    "Appraisal start date and end date are required"
            );
        }

        if (request.getAppraisalEndDate()
                .isBefore(request.getAppraisalStartDate())) {
            throw new IllegalArgumentException(
                    "Appraisal end date cannot be before start date"
            );
        }

        if (appraisalCycleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(
                    "Appraisal cycle already exists: " + request.getName()
            );
        }

        validateDateRange(
                request.getGoalSettingStartDate(),
                request.getGoalSettingEndDate(),
                "Goal setting"
        );

        validateDateRange(
                request.getSelfReviewStartDate(),
                request.getSelfReviewEndDate(),
                "Self review"
        );

        validateDateRange(
                request.getManagerReviewStartDate(),
                request.getManagerReviewEndDate(),
                "Manager review"
        );

        validateDateRange(
                request.getHrReviewStartDate(),
                request.getHrReviewEndDate(),
                "HR review"
        );

        AppraisalCycle cycle = AppraisalCycle.builder()
                .name(request.getName())
                .description(request.getDescription())
                .appraisalStartDate(request.getAppraisalStartDate())
                .appraisalEndDate(request.getAppraisalEndDate())
                .goalSettingStartDate(request.getGoalSettingStartDate())
                .goalSettingEndDate(request.getGoalSettingEndDate())
                .selfReviewStartDate(request.getSelfReviewStartDate())
                .selfReviewEndDate(request.getSelfReviewEndDate())
                .managerReviewStartDate(request.getManagerReviewStartDate())
                .managerReviewEndDate(request.getManagerReviewEndDate())
                .hrReviewStartDate(request.getHrReviewStartDate())
                .hrReviewEndDate(request.getHrReviewEndDate())
                .status(
                        request.getStatus() != null
                                ? request.getStatus()
                                : AppraisalCycleStatus.DRAFT
                )
                .createdBy(createdBy)
                .build();

        AppraisalCycle savedCycle =
                appraisalCycleRepository.save(cycle);

        /*
         * Automatically create an employee appraisal
         * for every active employee.
         */
        List<User> employees =
                userRepository.findByRoleAndActiveTrue(Role.EMPLOYEE);

        for (User employee : employees) {

            boolean exists =
                    employeeAppraisalRepository
                            .findByCycleIdAndEmployeeId(
                                    savedCycle.getId(),
                                    employee.getId()
                            )
                            .isPresent();

            if (!exists) {

            	EmployeeAppraisal appraisal =
            	        EmployeeAppraisal.builder()
            	                .cycle(savedCycle)
            	                .employeeId(employee.getId())
            	                .managerId(
            	                        employee.getManager() != null
            	                                ? employee.getManager().getId()
            	                                : null
            	                )
            	                .status(AppraisalStatus.DRAFT)
            	                .build();

                employeeAppraisalRepository.save(appraisal);
            }
        }

        return toResponse(savedCycle);
    }

    @Override
    public AppraisalCycleResponse updateCycle(
            Long cycleId,
            AppraisalCycleRequest request
    ) {

        AppraisalCycle cycle = getEntity(cycleId);

        if (request.getName() != null &&
                !request.getName().isBlank()) {

            appraisalCycleRepository.findByName(request.getName())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(cycleId)) {
                            throw new IllegalArgumentException(
                                    "Another appraisal cycle already uses this name"
                            );
                        }
                    });

            cycle.setName(request.getName());
        }

        if (request.getDescription() != null) {
            cycle.setDescription(request.getDescription());
        }

        if (request.getAppraisalStartDate() != null) {
            cycle.setAppraisalStartDate(
                    request.getAppraisalStartDate()
            );
        }

        if (request.getAppraisalEndDate() != null) {
            cycle.setAppraisalEndDate(
                    request.getAppraisalEndDate()
            );
        }

        validateMainDates(cycle);

        if (request.getGoalSettingStartDate() != null) {
            cycle.setGoalSettingStartDate(
                    request.getGoalSettingStartDate()
            );
        }

        if (request.getGoalSettingEndDate() != null) {
            cycle.setGoalSettingEndDate(
                    request.getGoalSettingEndDate()
            );
        }

        if (request.getSelfReviewStartDate() != null) {
            cycle.setSelfReviewStartDate(
                    request.getSelfReviewStartDate()
            );
        }

        if (request.getSelfReviewEndDate() != null) {
            cycle.setSelfReviewEndDate(
                    request.getSelfReviewEndDate()
            );
        }

        if (request.getManagerReviewStartDate() != null) {
            cycle.setManagerReviewStartDate(
                    request.getManagerReviewStartDate()
            );
        }

        if (request.getManagerReviewEndDate() != null) {
            cycle.setManagerReviewEndDate(
                    request.getManagerReviewEndDate()
            );
        }

        if (request.getHrReviewStartDate() != null) {
            cycle.setHrReviewStartDate(
                    request.getHrReviewStartDate()
            );
        }

        if (request.getHrReviewEndDate() != null) {
            cycle.setHrReviewEndDate(
                    request.getHrReviewEndDate()
            );
        }

        validateDateRange(
                cycle.getGoalSettingStartDate(),
                cycle.getGoalSettingEndDate(),
                "Goal setting"
        );

        validateDateRange(
                cycle.getSelfReviewStartDate(),
                cycle.getSelfReviewEndDate(),
                "Self review"
        );

        validateDateRange(
                cycle.getManagerReviewStartDate(),
                cycle.getManagerReviewEndDate(),
                "Manager review"
        );

        validateDateRange(
                cycle.getHrReviewStartDate(),
                cycle.getHrReviewEndDate(),
                "HR review"
        );

        if (request.getStatus() != null) {
            cycle.setStatus(request.getStatus());
        }

        return toResponse(appraisalCycleRepository.save(cycle));
    }

    @Override
    @Transactional(readOnly = true)
    public AppraisalCycleResponse getCycleById(
            Long cycleId
    ) {

        return toResponse(getEntity(cycleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppraisalCycleResponse> getAllCycles() {

        return appraisalCycleRepository
                .findAllByOrderByAppraisalStartDateDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AppraisalCycleResponse updateCycleStatus(
            Long cycleId,
            String status
    ) {

        AppraisalCycle cycle = getEntity(cycleId);

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Appraisal cycle status is required"
            );
        }

        final AppraisalCycleStatus newStatus;

        try {
            newStatus = AppraisalCycleStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid appraisal cycle status: " + status
            );
        }

        cycle.setStatus(newStatus);

        return toResponse(appraisalCycleRepository.save(cycle));
    }

    private AppraisalCycle getEntity(Long cycleId) {

        return appraisalCycleRepository.findById(cycleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Appraisal cycle not found: " + cycleId
                        )
                );
    }

    private void validateMainDates(
            AppraisalCycle cycle
    ) {

        if (cycle.getAppraisalStartDate() == null ||
                cycle.getAppraisalEndDate() == null) {
            throw new IllegalArgumentException(
                    "Appraisal start date and end date are required"
            );
        }

        if (cycle.getAppraisalEndDate()
                .isBefore(cycle.getAppraisalStartDate())) {
            throw new IllegalArgumentException(
                    "Appraisal end date cannot be before start date"
            );
        }
    }

    private void validateDateRange(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String stage
    ) {

        if (startDate != null &&
                endDate != null &&
                endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    stage + " end date cannot be before start date"
            );
        }
    }

    private AppraisalCycleResponse toResponse(
            AppraisalCycle cycle
    ) {

        return AppraisalCycleResponse.builder()
                .id(cycle.getId())
                .name(cycle.getName())
                .description(cycle.getDescription())
                .appraisalStartDate(cycle.getAppraisalStartDate())
                .appraisalEndDate(cycle.getAppraisalEndDate())
                .goalSettingStartDate(cycle.getGoalSettingStartDate())
                .goalSettingEndDate(cycle.getGoalSettingEndDate())
                .selfReviewStartDate(cycle.getSelfReviewStartDate())
                .selfReviewEndDate(cycle.getSelfReviewEndDate())
                .managerReviewStartDate(
                        cycle.getManagerReviewStartDate()
                )
                .managerReviewEndDate(
                        cycle.getManagerReviewEndDate()
                )
                .hrReviewStartDate(
                        cycle.getHrReviewStartDate()
                )
                .hrReviewEndDate(
                        cycle.getHrReviewEndDate()
                )
                .status(cycle.getStatus())
                .createdBy(cycle.getCreatedBy())
                .createdAt(cycle.getCreatedAt())
                .updatedAt(cycle.getUpdatedAt())
                .build();
    }
}
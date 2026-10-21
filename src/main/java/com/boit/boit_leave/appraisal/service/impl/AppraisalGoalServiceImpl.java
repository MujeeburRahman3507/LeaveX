package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.AppraisalGoalRequest;
import com.boit.boit_leave.appraisal.dto.AppraisalGoalResponse;
import com.boit.boit_leave.appraisal.entity.AppraisalGoal;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.enums.AppraisalGoalStatus;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.AppraisalGoalRepository;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.appraisal.service.AppraisalGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppraisalGoalServiceImpl implements AppraisalGoalService {

    private final AppraisalGoalRepository appraisalGoalRepository;
    private final EmployeeAppraisalRepository employeeAppraisalRepository;

    @Override
    public AppraisalGoalResponse createGoal(
            AppraisalGoalRequest request,
            Long employeeId
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Goal request is required"
            );
        }

        if (request.getAppraisalId() == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        EmployeeAppraisal appraisal =
                getAppraisal(request.getAppraisalId());

        validateEmployeeOwnership(
                appraisal,
                employeeId
        );

        validateGoalModificationStatus(appraisal);

        validateGoalRequest(request);

        validateTotalWeight(
                request.getAppraisalId(),
                request.getWeight(),
                null
        );

        AppraisalGoal goal = AppraisalGoal.builder()
                .appraisal(appraisal)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .category(request.getCategory())
                .target(request.getTarget())
                .measurement(request.getMeasurement())
                .weight(request.getWeight())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(
                        AppraisalGoalStatus.DRAFT
                )
                .createdBy(employeeId)
                .build();

        return toResponse(
                appraisalGoalRepository.save(goal)
        );
    }

    @Override
    public AppraisalGoalResponse updateGoal(
            Long goalId,
            AppraisalGoalRequest request,
            Long employeeId
    ) {

        AppraisalGoal goal = getGoal(goalId);

        EmployeeAppraisal appraisal = goal.getAppraisal();

        validateEmployeeOwnership(
                appraisal,
                employeeId
        );

        validateGoalModificationStatus(appraisal);

        if (goal.getStatus() != AppraisalGoalStatus.DRAFT &&
                goal.getStatus() != AppraisalGoalStatus.REVISION_REQUIRED) {

            throw new IllegalStateException(
                    "Goal cannot be updated in status: "
                            + goal.getStatus()
            );
        }

        validateGoalRequest(request);

        validateTotalWeight(
                appraisal.getId(),
                request.getWeight(),
                goalId
        );

        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription());
        goal.setCategory(request.getCategory());
        goal.setTarget(request.getTarget());
        goal.setMeasurement(request.getMeasurement());
        goal.setWeight(request.getWeight());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());

        return toResponse(
                appraisalGoalRepository.save(goal)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AppraisalGoalResponse getGoalById(
            Long goalId
    ) {

        return toResponse(
                getGoal(goalId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppraisalGoalResponse> getGoalsByAppraisal(
            Long appraisalId
    ) {

        if (!employeeAppraisalRepository.existsById(appraisalId)) {
            throw new IllegalArgumentException(
                    "Appraisal not found: " + appraisalId
            );
        }

        return appraisalGoalRepository
                .findByAppraisalIdOrderByIdAsc(appraisalId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteGoal(
            Long goalId,
            Long employeeId
    ) {

        AppraisalGoal goal = getGoal(goalId);

        EmployeeAppraisal appraisal = goal.getAppraisal();

        validateEmployeeOwnership(
                appraisal,
                employeeId
        );

        validateGoalModificationStatus(appraisal);

        if (goal.getStatus() != AppraisalGoalStatus.DRAFT &&
                goal.getStatus() != AppraisalGoalStatus.REVISION_REQUIRED) {

            throw new IllegalStateException(
                    "Goal cannot be deleted in status: "
                            + goal.getStatus()
            );
        }

        appraisalGoalRepository.delete(goal);
    }

    private EmployeeAppraisal getAppraisal(
            Long appraisalId
    ) {

        return employeeAppraisalRepository
                .findById(appraisalId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Appraisal not found: "
                                        + appraisalId
                        )
                );
    }

    private AppraisalGoal getGoal(
            Long goalId
    ) {

        return appraisalGoalRepository
                .findById(goalId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Goal not found: " + goalId
                        )
                );
    }

    private void validateEmployeeOwnership(
            EmployeeAppraisal appraisal,
            Long employeeId
    ) {

        if (employeeId == null ||
                appraisal.getEmployeeId() == null ||
                !employeeId.equals(appraisal.getEmployeeId())) {

            throw new IllegalStateException(
                    "You are not authorized to modify this goal"
            );
        }
    }

    private void validateGoalModificationStatus(
            EmployeeAppraisal appraisal
    ) {

        AppraisalStatus status = appraisal.getStatus();

        if (status != AppraisalStatus.DRAFT &&
                status != AppraisalStatus.GOALS_REVISION_REQUIRED) {

            throw new IllegalStateException(
                    "Goals cannot be modified when appraisal status is: "
                            + status
            );
        }
    }

    private void validateGoalRequest(
            AppraisalGoalRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Goal request is required"
            );
        }

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Goal title is required"
            );
        }

        if (request.getWeight() == null) {
            throw new IllegalArgumentException(
                    "Goal weight is required"
            );
        }

        if (request.getWeight().compareTo(BigDecimal.ZERO) < 0 ||
                request.getWeight().compareTo(
                        new BigDecimal("100")
                ) > 0) {

            throw new IllegalArgumentException(
                    "Goal weight must be between 0 and 100"
            );
        }

        if (request.getStartDate() != null &&
                request.getEndDate() != null &&
                request.getEndDate().isBefore(
                        request.getStartDate()
                )) {

            throw new IllegalArgumentException(
                    "Goal end date cannot be before start date"
            );
        }
    }

    private void validateTotalWeight(
            Long appraisalId,
            BigDecimal newWeight,
            Long existingGoalId
    ) {

        BigDecimal currentTotal =
                appraisalGoalRepository
                        .sumWeightByAppraisalId(appraisalId);

        if (currentTotal == null) {
            currentTotal = BigDecimal.ZERO;
        }

        if (existingGoalId != null) {

            AppraisalGoal existingGoal =
                    appraisalGoalRepository
                            .findById(existingGoalId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Existing goal not found"
                                    )
                            );

            currentTotal = currentTotal.subtract(
                    existingGoal.getWeight() != null
                            ? existingGoal.getWeight()
                            : BigDecimal.ZERO
            );
        }

        BigDecimal finalTotal =
                currentTotal.add(newWeight);

        if (finalTotal.compareTo(
                new BigDecimal("100")
        ) > 0) {

            throw new IllegalArgumentException(
                    "Total goal weight cannot exceed 100%. "
                            + "Current total: "
                            + currentTotal
                            + "%, requested goal: "
                            + newWeight
                            + "%"
            );
        }
    }

    private AppraisalGoalResponse toResponse(
            AppraisalGoal goal
    ) {

        return AppraisalGoalResponse.builder()
                .id(goal.getId())
                .appraisalId(
                        goal.getAppraisal() != null
                                ? goal.getAppraisal().getId()
                                : null
                )
                .title(goal.getTitle())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .target(goal.getTarget())
                .measurement(goal.getMeasurement())
                .weight(goal.getWeight())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .status(goal.getStatus())
                .createdBy(goal.getCreatedBy())
                .build();
    }
}
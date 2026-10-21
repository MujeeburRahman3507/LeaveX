package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.GoalProgressRequest;
import com.boit.boit_leave.appraisal.dto.GoalProgressResponse;
import com.boit.boit_leave.appraisal.entity.AppraisalGoal;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.entity.GoalProgress;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.AppraisalGoalRepository;
import com.boit.boit_leave.appraisal.repository.GoalProgressRepository;
import com.boit.boit_leave.appraisal.service.GoalProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalProgressServiceImpl implements GoalProgressService {

    private final GoalProgressRepository goalProgressRepository;
    private final AppraisalGoalRepository appraisalGoalRepository;

    @Override
    public GoalProgressResponse updateProgress(
            Long goalId,
            GoalProgressRequest request,
            Long employeeId
    ) {

        if (goalId == null) {
            throw new IllegalArgumentException(
                    "Goal ID is required"
            );
        }

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "Employee ID is required"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Progress request is required"
            );
        }

        AppraisalGoal goal = appraisalGoalRepository
                .findById(goalId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Goal not found: " + goalId
                        )
                );

        EmployeeAppraisal appraisal = goal.getAppraisal();

        if (appraisal == null) {
            throw new IllegalStateException(
                    "Goal is not linked to an appraisal"
            );
        }

        if (!employeeId.equals(appraisal.getEmployeeId())) {
            throw new IllegalStateException(
                    "You are not authorized to update this goal"
            );
        }

        validateProgressRequest(request);

        validateAppraisalStatus(appraisal);

        GoalProgress progress = GoalProgress.builder()
                .goal(goal)
                .progressPercentage(
                        request.getProgressPercentage()
                )
                .status(
                        request.getStatus() != null &&
                                !request.getStatus().isBlank()
                                ? request.getStatus().toUpperCase()
                                : "IN_PROGRESS"
                )
                .employeeComment(
                        request.getEmployeeComment()
                )
                .updatedBy(employeeId)
                .build();

        GoalProgress saved =
                goalProgressRepository.save(progress);
        
        if (appraisal.getStatus() == AppraisalStatus.GOALS_APPROVED) {
            appraisal.setStatus(AppraisalStatus.IN_PROGRESS);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalProgressResponse getLatestProgress(
            Long goalId
    ) {

        if (goalId == null) {
            throw new IllegalArgumentException(
                    "Goal ID is required"
            );
        }

        return goalProgressRepository
                .findFirstByGoalIdOrderByUpdatedAtDesc(goalId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalProgressResponse> getProgressHistory(
            Long goalId
    ) {

        if (goalId == null) {
            throw new IllegalArgumentException(
                    "Goal ID is required"
            );
        }

        return goalProgressRepository
                .findByGoalIdOrderByUpdatedAtDesc(goalId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateProgressRequest(
            GoalProgressRequest request
    ) {

        if (request.getProgressPercentage() == null) {
            throw new IllegalArgumentException(
                    "Progress percentage is required"
            );
        }

        BigDecimal percentage =
                request.getProgressPercentage();

        if (percentage.compareTo(BigDecimal.ZERO) < 0 ||
                percentage.compareTo(
                        new BigDecimal("100")
                ) > 0) {

            throw new IllegalArgumentException(
                    "Progress percentage must be between 0 and 100"
            );
        }

        if (request.getStatus() != null &&
                !request.getStatus().isBlank()) {

            String status =
                    request.getStatus().toUpperCase();

            if (!status.equals("NOT_STARTED") &&
                    !status.equals("IN_PROGRESS") &&
                    !status.equals("COMPLETED") &&
                    !status.equals("CANCELLED")) {

                throw new IllegalArgumentException(
                        "Invalid progress status: "
                                + request.getStatus()
                );
            }
        }
    }

    private void validateAppraisalStatus(
            EmployeeAppraisal appraisal
    ) {

        AppraisalStatus status =
                appraisal.getStatus();

        /*
         * Progress can be updated while the appraisal
         * is in the goal/in-progress stages.
         *
         * Once employee self-review or later stages
         * have started, goal progress is locked.
         */
        if (status != AppraisalStatus.GOALS_APPROVED &&
                status != AppraisalStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Goal progress cannot be updated when appraisal status is: "
                            + status
            );
        }
    }

    private GoalProgressResponse toResponse(
            GoalProgress progress
    ) {

        return GoalProgressResponse.builder()
                .id(progress.getId())
                .goalId(
                        progress.getGoal() != null
                                ? progress.getGoal().getId()
                                : null
                )
                .progressPercentage(
                        progress.getProgressPercentage()
                )
                .status(progress.getStatus())
                .employeeComment(
                        progress.getEmployeeComment()
                )
                .updatedBy(progress.getUpdatedBy())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
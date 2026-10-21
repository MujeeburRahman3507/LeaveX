package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.EmployeeAppraisalResponse;
import com.boit.boit_leave.appraisal.entity.AppraisalCycle;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.enums.AppraisalGoalStatus;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.AppraisalCycleRepository;
import com.boit.boit_leave.appraisal.repository.AppraisalGoalRepository;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.appraisal.service.EmployeeAppraisalService;
import com.boit.boit_leave.appraisal.enums.AppraisalCycleStatus;
import com.boit.boit_leave.service.NotificationService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAppraisalServiceImpl
        implements EmployeeAppraisalService {

    private final EmployeeAppraisalRepository employeeAppraisalRepository;
    private final AppraisalCycleRepository appraisalCycleRepository;
    private final AppraisalGoalRepository appraisalGoalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    

    @Override
    @Transactional
    public EmployeeAppraisalResponse createAppraisal(
            Long cycleId,
            Long employeeId
    ) {

        AppraisalCycle cycle = appraisalCycleRepository
                .findById(cycleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Appraisal cycle not found: " + cycleId
                        )
                );

        User employee = userRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );

        Optional<EmployeeAppraisal> existing =
                employeeAppraisalRepository
                        .findByCycleIdAndEmployeeId(
                                cycleId,
                                employeeId
                        );

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Appraisal already exists for this employee and cycle."
            );
        }

        if (cycle.getStatus() == AppraisalCycleStatus.CLOSED) {
            throw new IllegalStateException(
                    "Cannot create appraisal for a closed cycle."
            );
        }

        Long managerId = null;

        if (employee.getManager() != null) {
            managerId = employee.getManager().getId();
        }

        EmployeeAppraisal appraisal =
                EmployeeAppraisal.builder()
                        .cycle(cycle)
                        .employeeId(employeeId)
                        .managerId(managerId)
                        .status(AppraisalStatus.DRAFT)
                        .build();

        EmployeeAppraisal saved =
                employeeAppraisalRepository.save(appraisal);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAppraisalResponse getById(
            Long appraisalId
    ) {

        return toResponse(getEntity(appraisalId));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAppraisalResponse getEmployeeAppraisal(
            Long cycleId,
            Long employeeId
    ) {

        EmployeeAppraisal appraisal =
                employeeAppraisalRepository
                        .findByCycleIdAndEmployeeId(
                                cycleId,
                                employeeId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Employee appraisal not found"
                                )
                        );

        return toResponse(appraisal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAppraisalResponse> getEmployeeAppraisals(
            Long employeeId
    ) {

        return employeeAppraisalRepository
                .findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAppraisalResponse> getManagerAppraisals(
            Long managerId
    ) {

        return employeeAppraisalRepository
                .findByManagerIdOrderByCreatedAtDesc(managerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAppraisalResponse> getAllAppraisals() {

        return employeeAppraisalRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EmployeeAppraisalResponse submitGoals(
            Long appraisalId,
            Long employeeId
    ) {

        EmployeeAppraisal appraisal = getEntity(appraisalId);

        validateEmployeeOwnership(
                appraisal,
                employeeId
        );

        if (appraisal.getStatus() != AppraisalStatus.DRAFT &&
                appraisal.getStatus() != AppraisalStatus.GOALS_REVISION_REQUIRED) {

            throw new IllegalStateException(
                    "Goals cannot be submitted from status: "
                            + appraisal.getStatus()
            );
        }

        long goalCount =
                appraisalGoalRepository.countByAppraisalId(appraisalId);

        if (goalCount == 0) {
            throw new IllegalStateException(
                    "At least one goal is required before submission"
            );
        }

        var totalWeight =
                appraisalGoalRepository
                        .sumWeightByAppraisalId(appraisalId);

        if (totalWeight == null ||
                totalWeight.compareTo(
                        new java.math.BigDecimal("100")
                ) != 0) {

            throw new IllegalStateException(
                    "Total goal weight must be exactly 100%"
            );
        }

        boolean invalidGoalStatus =
                appraisalGoalRepository
                        .findByAppraisalIdOrderByIdAsc(appraisalId)
                        .stream()
                        .anyMatch(goal ->
                                goal.getStatus() != AppraisalGoalStatus.DRAFT &&
                                goal.getStatus() != AppraisalGoalStatus.REVISION_REQUIRED
                        );

        if (invalidGoalStatus) {
            throw new IllegalStateException(
                    "One or more goals cannot be submitted in their current status"
            );
        }

        appraisalGoalRepository
        .findByAppraisalIdOrderByIdAsc(appraisalId)
        .forEach(goal ->
                goal.setStatus(AppraisalGoalStatus.SUBMITTED)
        );

appraisal.setStatus(AppraisalStatus.GOALS_SUBMITTED);

EmployeeAppraisal saved =
        employeeAppraisalRepository.save(appraisal);

// ---------------------------------------------------------
// Notify Manager: Goals Submitted
// ---------------------------------------------------------

if (saved.getManagerId() != null) {

    User employee = userRepository
            .findById(saved.getEmployeeId())
            .orElse(null);

    if (employee != null) {

        String cycleName =
                saved.getCycle().getName();

        User manager = userRepository
                .findById(saved.getManagerId())
                .orElse(null);

        if (manager != null) {

            notificationService
                    .sendTemplateNotificationAndEmail(
                            manager.getId(),
                            "APPRAISAL_GOALS_SUBMITTED",
                            Map.of(
                                    "employeeName",
                                    employee.getFullName(),

                                    "managerName",
                                    manager.getFullName(),

                                    "cycleName",
                                    cycleName
                            )
                    );
        }
    }
}

return toResponse(saved);
    }

    @Override
    public EmployeeAppraisalResponse approveGoals(
            Long appraisalId,
            Long managerId
    ) {

        EmployeeAppraisal appraisal = getEntity(appraisalId);

        validateManagerOwnership(
                appraisal,
                managerId
        );

        if (appraisal.getStatus() !=
                AppraisalStatus.GOALS_SUBMITTED) {

            throw new IllegalStateException(
                    "Goals cannot be approved from status: "
                            + appraisal.getStatus()
            );
        }

        appraisalGoalRepository
                .findByAppraisalIdOrderByIdAsc(appraisalId)
                .forEach(goal ->
                        goal.setStatus(AppraisalGoalStatus.APPROVED)
                );

        appraisal.setStatus(AppraisalStatus.GOALS_APPROVED);

        EmployeeAppraisal saved =
                employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Notify Employee: Goals Approved
        // ---------------------------------------------------------

        User employee = userRepository
                .findById(saved.getEmployeeId())
                .orElse(null);

        User manager = userRepository
                .findById(managerId)
                .orElse(null);

        if (employee != null) {

            String managerName =
                    manager != null
                            ? manager.getFullName()
                            : "";

            notificationService
                    .sendTemplateNotificationAndEmail(
                            employee.getId(),
                            "APPRAISAL_GOALS_APPROVED",
                            Map.of(
                                    "employeeName",
                                    employee.getFullName(),

                                    "managerName",
                                    managerName,

                                    "cycleName",
                                    saved.getCycle().getName()
                            )
                    );
        }

        return toResponse(saved);
    }

    @Override
    public EmployeeAppraisalResponse requestGoalRevision(
            Long appraisalId,
            Long managerId
    ) {

        EmployeeAppraisal appraisal = getEntity(appraisalId);

        validateManagerOwnership(
                appraisal,
                managerId
        );

        if (appraisal.getStatus() !=
                AppraisalStatus.GOALS_SUBMITTED) {

            throw new IllegalStateException(
                    "Goal revision cannot be requested from status: "
                            + appraisal.getStatus()
            );
        }

        appraisalGoalRepository
                .findByAppraisalIdOrderByIdAsc(appraisalId)
                .forEach(goal ->
                        goal.setStatus(
                                AppraisalGoalStatus.REVISION_REQUIRED
                        )
                );

        appraisal.setStatus(
                AppraisalStatus.GOALS_REVISION_REQUIRED
        );

        EmployeeAppraisal saved =
                employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Notify Employee: Goal Revision Required
        // ---------------------------------------------------------

        User employee = userRepository
                .findById(saved.getEmployeeId())
                .orElse(null);

        User manager = userRepository
                .findById(managerId)
                .orElse(null);

        if (employee != null) {

            String managerName =
                    manager != null
                            ? manager.getFullName()
                            : "";

            notificationService
                    .sendTemplateNotificationAndEmail(
                            employee.getId(),
                            "APPRAISAL_GOAL_REVISION",
                            Map.of(
                                    "employeeName",
                                    employee.getFullName(),

                                    "managerName",
                                    managerName,

                                    "cycleName",
                                    saved.getCycle().getName(),

                                    "managerComment",
                                    ""
                            )
                    );
        }

        return toResponse(saved);
    }

    private EmployeeAppraisal getEntity(
            Long appraisalId
    ) {

        return employeeAppraisalRepository
                .findById(appraisalId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee appraisal not found: "
                                        + appraisalId
                        )
                );
    }

    private void validateEmployeeOwnership(
            EmployeeAppraisal appraisal,
            Long employeeId
    ) {

        if (employeeId == null ||
                !employeeId.equals(appraisal.getEmployeeId())) {

            throw new IllegalStateException(
                    "You are not authorized to modify this appraisal"
            );
        }
    }

    private void validateManagerOwnership(
            EmployeeAppraisal appraisal,
            Long managerId
    ) {

        if (managerId == null ||
                appraisal.getManagerId() == null ||
                !managerId.equals(appraisal.getManagerId())) {

            throw new IllegalStateException(
                    "You are not authorized to review this appraisal"
            );
        }
    }

    private EmployeeAppraisalResponse toResponse(
            EmployeeAppraisal appraisal
    ) {

        String employeeName = null;
        String managerName = null;

        if (appraisal.getEmployeeId() != null) {

            employeeName = userRepository
                    .findById(appraisal.getEmployeeId())
                    .map(User::getFullName)
                    .orElse(null);
        }

        if (appraisal.getManagerId() != null) {

            managerName = userRepository
                    .findById(appraisal.getManagerId())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return EmployeeAppraisalResponse.builder()
                .id(appraisal.getId())
                .cycleId(appraisal.getCycle().getId())
                .cycleName(appraisal.getCycle().getName())
                .employeeId(appraisal.getEmployeeId())
                .employeeName(employeeName)
                .managerId(appraisal.getManagerId())
                .managerName(managerName)
                .status(appraisal.getStatus())
                .overallEmployeeRating(
                        appraisal.getOverallEmployeeRating()
                )
                .overallManagerRating(
                        appraisal.getOverallManagerRating()
                )
                .finalRating(
                        appraisal.getFinalRating()
                )
                .finalPerformanceCategory(
                        appraisal.getFinalPerformanceCategory()
                )
                .createdAt(appraisal.getCreatedAt())
                .updatedAt(appraisal.getUpdatedAt())
                .build();
    }
    
    @Override
    @Transactional
    public void startSelfReview(
            Long appraisalId,
            Long employeeId
    ) {

        EmployeeAppraisal appraisal =
                employeeAppraisalRepository.findById(appraisalId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appraisal not found"
                                )
                        );

        // Validate employee ownership
        if (appraisal.getEmployeeId() == null ||
                !appraisal.getEmployeeId().equals(employeeId)) {

            throw new RuntimeException(
                    "You are not authorized to start self review"
            );
        }

        // Self review can only start from IN_PROGRESS
        if (appraisal.getStatus() !=
                AppraisalStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Self review can only be started when appraisal is IN_PROGRESS"
            );
        }

        appraisal.setStatus(
                AppraisalStatus.SELF_REVIEW
        );

        employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Send notification + email to employee
        // ---------------------------------------------------------

        User employee =
                userRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                )
                        );

        String cycleName =
                appraisal.getCycle().getName();

        notificationService.sendTemplateNotificationAndEmail(
                employee.getId(),
                "APPRAISAL_SELF_REVIEW_STARTED",
                Map.of(
                        "employeeName",
                        employee.getFullName(),

                        "cycleName",
                        cycleName
                )
        );
    }
}
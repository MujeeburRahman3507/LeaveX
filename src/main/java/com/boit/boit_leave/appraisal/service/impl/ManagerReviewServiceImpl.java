package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.ManagerReviewRequest;
import com.boit.boit_leave.appraisal.dto.ManagerReviewResponse;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.entity.ManagerReview;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.appraisal.repository.ManagerReviewRepository;
import com.boit.boit_leave.appraisal.service.ManagerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.service.NotificationService;

import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerReviewServiceImpl implements ManagerReviewService {

	private final ManagerReviewRepository managerReviewRepository;

	private final EmployeeAppraisalRepository employeeAppraisalRepository;

	private final UserRepository userRepository;

	private final NotificationService notificationService;

    @Override
    public ManagerReviewResponse saveReview(
            ManagerReviewRequest request,
            Long managerId
    ) {

        if (request == null ||
                request.getAppraisalId() == null) {

            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        EmployeeAppraisal appraisal =
                getAppraisal(request.getAppraisalId());

        validateManagerOwnership(
                appraisal,
                managerId
        );

        validateReviewEditingStatus(appraisal);

        validateRating(request.getOverallRating());

        ManagerReview review =
                managerReviewRepository
                        .findByAppraisalId(appraisal.getId())
                        .orElseGet(() ->
                                ManagerReview.builder()
                                        .appraisal(appraisal)
                                        .status("DRAFT")
                                        .build()
                        );

        review.setOverallRating(
                request.getOverallRating()
        );

        review.setOverallComments(
                request.getOverallComments()
        );

        review.setStatus("DRAFT");

        ManagerReview saved =
                managerReviewRepository.save(review);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerReviewResponse getReview(
            Long appraisalId
    ) {

        if (appraisalId == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        return managerReviewRepository
                .findByAppraisalId(appraisalId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public ManagerReviewResponse submitReview(
            Long appraisalId,
            Long managerId
    ) {

        EmployeeAppraisal appraisal =
                getAppraisal(appraisalId);

        validateManagerOwnership(
                appraisal,
                managerId
        );

        if (appraisal.getStatus() !=
                AppraisalStatus.MANAGER_REVIEW) {

            throw new IllegalStateException(
                    "Manager review cannot be submitted when appraisal status is: "
                            + appraisal.getStatus()
            );
        }

        ManagerReview review =
                managerReviewRepository
                        .findByAppraisalId(appraisalId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Manager review has not been created"
                                )
                        );

        if (review.getOverallRating() == null) {

            throw new IllegalStateException(
                    "Overall manager rating is required"
            );
        }

        validateRating(
                review.getOverallRating()
        );

        if (review.getOverallComments() == null ||
                review.getOverallComments().isBlank()) {

            throw new IllegalStateException(
                    "Manager comments are required before submission"
            );
        }

        review.setStatus("SUBMITTED");

        review.setSubmittedBy(managerId);

        review.setSubmittedAt(LocalDateTime.now());

        appraisal.setOverallManagerRating(
                review.getOverallRating()
        );

        appraisal.setStatus(
                AppraisalStatus.MANAGER_SUBMITTED
        );

        managerReviewRepository.save(review);

        EmployeeAppraisal savedAppraisal =
                employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Notify HR / Admin: Manager Review Submitted
        // ---------------------------------------------------------

        User employee =
                userRepository.findById(
                        savedAppraisal.getEmployeeId()
                ).orElse(null);

        User manager =
                userRepository.findById(
                        managerId
                ).orElse(null);

        if (employee != null && manager != null) {

            User admin =
                    userRepository
                            .findByRole(
                                    com.boit.boit_leave.entity.Role.ADMIN
                            )
                            .stream()
                            .findFirst()
                            .orElse(null);

            if (admin != null) {

                String cycleName =
                        savedAppraisal.getCycle().getName();

                notificationService
                        .sendTemplateNotificationAndEmail(
                                admin.getId(),
                                "APPRAISAL_MANAGER_REVIEW_SUBMITTED",
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

        return toResponse(review);
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

    private void validateManagerOwnership(
            EmployeeAppraisal appraisal,
            Long managerId
    ) {

        if (managerId == null ||
                appraisal.getManagerId() == null ||
                !managerId.equals(
                        appraisal.getManagerId()
                )) {

            throw new IllegalStateException(
                    "You are not authorized to review this appraisal"
            );
        }
    }

    private void validateReviewEditingStatus(
            EmployeeAppraisal appraisal
    ) {

        if (appraisal.getStatus() !=
                AppraisalStatus.MANAGER_REVIEW) {

            throw new IllegalStateException(
                    "Manager review cannot be edited when appraisal status is: "
                            + appraisal.getStatus()
            );
        }
    }

    private void validateRating(
            BigDecimal rating
    ) {

        if (rating == null) {
            return;
        }

        if (rating.compareTo(
                BigDecimal.ONE
        ) < 0 ||
                rating.compareTo(
                        new BigDecimal("5")
                ) > 0) {

            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }
    }

    private ManagerReviewResponse toResponse(
            ManagerReview review
    ) {

        return ManagerReviewResponse.builder()
                .id(review.getId())
                .appraisalId(
                        review.getAppraisal() != null
                                ? review.getAppraisal().getId()
                                : null
                )
                .overallRating(
                        review.getOverallRating()
                )
                .overallComments(
                        review.getOverallComments()
                )
                .status(review.getStatus())
                .submittedBy(
                        review.getSubmittedBy()
                )
                .submittedAt(
                        review.getSubmittedAt()
                )
                .createdAt(
                        review.getCreatedAt()
                )
                .updatedAt(
                        review.getUpdatedAt()
                )
                .build();
    }
    
    @Override
    public void startReview(
            Long appraisalId,
            Long managerId
    ) {

        if (appraisalId == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        if (managerId == null) {
            throw new IllegalArgumentException(
                    "Manager ID is required"
            );
        }

        EmployeeAppraisal appraisal =
                getAppraisal(appraisalId);

        validateManagerOwnership(
                appraisal,
                managerId
        );

        if (appraisal.getStatus() !=
                AppraisalStatus.EMPLOYEE_SUBMITTED) {

            throw new IllegalStateException(
                    "Manager review cannot be started when appraisal status is: "
                            + appraisal.getStatus()
            );
        }

        appraisal.setStatus(
                AppraisalStatus.MANAGER_REVIEW
        );

        employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Send notification + email to manager
        // ---------------------------------------------------------

        User employee =
                userRepository.findById(
                        appraisal.getEmployeeId()
                ).orElse(null);

        User manager =
                userRepository.findById(
                        managerId
                ).orElse(null);

        if (employee != null && manager != null) {

            String cycleName =
                    appraisal.getCycle().getName();

            notificationService
                    .sendTemplateNotificationAndEmail(
                            manager.getId(),
                            "APPRAISAL_MANAGER_REVIEW_STARTED",
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
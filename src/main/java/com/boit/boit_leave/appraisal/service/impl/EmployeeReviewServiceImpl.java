package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.EmployeeReviewRequest;
import com.boit.boit_leave.appraisal.dto.EmployeeReviewResponse;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.entity.EmployeeReview;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.appraisal.repository.EmployeeReviewRepository;
import com.boit.boit_leave.appraisal.service.EmployeeReviewService;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.service.NotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeReviewServiceImpl
        implements EmployeeReviewService {

	private final EmployeeReviewRepository employeeReviewRepository;

	private final EmployeeAppraisalRepository employeeAppraisalRepository;

	private final UserRepository userRepository;

	private final NotificationService notificationService;

    @Override
    public EmployeeReviewResponse saveReview(
            EmployeeReviewRequest request,
            Long employeeId
    ) {

        if (request == null ||
                request.getAppraisalId() == null) {

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

        validateReviewEditingStatus(appraisal);

        validateRating(request.getOverallRating());

        EmployeeReview review =
                employeeReviewRepository
                        .findByAppraisalId(appraisal.getId())
                        .orElseGet(() ->
                                EmployeeReview.builder()
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

        EmployeeReview saved =
                employeeReviewRepository.save(review);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeReviewResponse getReview(
            Long appraisalId
    ) {

        if (appraisalId == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        return employeeReviewRepository
                .findByAppraisalId(appraisalId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public EmployeeReviewResponse submitReview(
            Long appraisalId,
            Long employeeId
    ) {

        EmployeeAppraisal appraisal =
                getAppraisal(appraisalId);

        validateEmployeeOwnership(
                appraisal,
                employeeId
        );

        if (appraisal.getStatus() !=
                AppraisalStatus.SELF_REVIEW) {

            throw new IllegalStateException(
                    "Self review cannot be submitted when appraisal status is: "
                            + appraisal.getStatus()
            );
        }

        EmployeeReview review =
                employeeReviewRepository
                        .findByAppraisalId(appraisalId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Employee self-review has not been created"
                                )
                        );

        if (review.getOverallRating() == null) {

            throw new IllegalStateException(
                    "Overall employee rating is required"
            );
        }

        validateRating(
                review.getOverallRating()
        );

        if (review.getOverallComments() == null ||
                review.getOverallComments().isBlank()) {

            throw new IllegalStateException(
                    "Employee comments are required before submission"
            );
        }

        review.setStatus("SUBMITTED");

        review.setSubmittedBy(employeeId);

        review.setSubmittedAt(LocalDateTime.now());

        appraisal.setOverallEmployeeRating(
                review.getOverallRating()
        );

        appraisal.setStatus(
                AppraisalStatus.EMPLOYEE_SUBMITTED
        );

        employeeReviewRepository.save(review);

        EmployeeAppraisal savedAppraisal =
                employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Notify Manager: Employee Self-Review Submitted
        // ---------------------------------------------------------

        if (savedAppraisal.getManagerId() != null) {

            User employee = userRepository
                    .findById(savedAppraisal.getEmployeeId())
                    .orElse(null);

            User manager = userRepository
                    .findById(savedAppraisal.getManagerId())
                    .orElse(null);

            if (employee != null && manager != null) {

                String cycleName =
                        savedAppraisal.getCycle().getName();

                notificationService
                        .sendTemplateNotificationAndEmail(
                                manager.getId(),
                                "APPRAISAL_SELF_REVIEW_SUBMITTED",
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

    private void validateEmployeeOwnership(
            EmployeeAppraisal appraisal,
            Long employeeId
    ) {

        if (employeeId == null ||
                appraisal.getEmployeeId() == null ||
                !employeeId.equals(
                        appraisal.getEmployeeId()
                )) {

            throw new IllegalStateException(
                    "You are not authorized to access this self-review"
            );
        }
    }

    private void validateReviewEditingStatus(
            EmployeeAppraisal appraisal
    ) {

        AppraisalStatus status =
                appraisal.getStatus();

        if (status != AppraisalStatus.SELF_REVIEW) {

            throw new IllegalStateException(
                    "Self-review cannot be edited when appraisal status is: "
                            + status
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

    private EmployeeReviewResponse toResponse(
            EmployeeReview review
    ) {

        return EmployeeReviewResponse.builder()
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
}
package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.HrReviewRequest;
import com.boit.boit_leave.appraisal.dto.HrReviewResponse;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.entity.HrReview;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.appraisal.repository.HrReviewRepository;
import com.boit.boit_leave.appraisal.service.HrReviewService;
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
public class HrReviewServiceImpl implements HrReviewService {

	private final HrReviewRepository hrReviewRepository;

	private final EmployeeAppraisalRepository employeeAppraisalRepository;

	private final UserRepository userRepository;

	private final NotificationService notificationService;

    @Override
    public HrReviewResponse saveReview(
            HrReviewRequest request,
            Long adminId
    ) {

        validateAdmin(adminId);

        if (request == null ||
                request.getAppraisalId() == null) {

            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        EmployeeAppraisal appraisal =
                getAppraisal(request.getAppraisalId());

        validateHrReviewStatus(appraisal);

        validateRating(request.getFinalRating());

        if (request.getPerformanceCategory() == null ||
                request.getPerformanceCategory().isBlank()) {

            throw new IllegalArgumentException(
                    "Performance category is required"
            );
        }

        HrReview review =
                hrReviewRepository
                        .findByAppraisalId(appraisal.getId())
                        .orElseGet(() ->
                                HrReview.builder()
                                        .appraisal(appraisal)
                                        .status("DRAFT")
                                        .build()
                        );

        review.setFinalRating(
                request.getFinalRating()
        );

        review.setPerformanceCategory(
                request.getPerformanceCategory()
        );

        review.setComments(
                request.getComments()
        );

        review.setStatus("DRAFT");

        HrReview saved =
                hrReviewRepository.save(review);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HrReviewResponse getReview(
            Long appraisalId
    ) {

        if (appraisalId == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        return hrReviewRepository
                .findByAppraisalId(appraisalId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public HrReviewResponse submitReview(
            Long appraisalId,
            Long adminId
    ) {

        validateAdmin(adminId);

        EmployeeAppraisal appraisal =
                getAppraisal(appraisalId);

        if (appraisal.getStatus() !=
                AppraisalStatus.HR_REVIEW) {

            throw new IllegalStateException(
                    "HR review cannot be submitted when appraisal status is: "
                            + appraisal.getStatus()
            );
        }

        HrReview review =
                hrReviewRepository
                        .findByAppraisalId(appraisalId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HR review has not been created"
                                )
                        );

        if (review.getFinalRating() == null) {

            throw new IllegalStateException(
                    "Final rating is required"
            );
        }

        validateRating(
                review.getFinalRating()
        );

        if (review.getPerformanceCategory() == null ||
                review.getPerformanceCategory().isBlank()) {

            throw new IllegalStateException(
                    "Performance category is required"
            );
        }

        review.setStatus("SUBMITTED");

        review.setSubmittedBy(adminId);

        review.setSubmittedAt(LocalDateTime.now());

        appraisal.setFinalRating(
                review.getFinalRating()
        );

        appraisal.setFinalPerformanceCategory(
                review.getPerformanceCategory()
        );

        appraisal.setStatus(
                AppraisalStatus.HR_SUBMITTED
        );

        hrReviewRepository.save(review);

        EmployeeAppraisal savedAppraisal =
                employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Notify Employee: HR Review Submitted
        // ---------------------------------------------------------
        //
        // There is currently no dedicated
        // APPRAISAL_HR_REVIEW_SUBMITTED template in the
        // existing 10-template set.
        //
        // Therefore, no notification is sent here yet.
        //
        // We will handle the employee notification as part of
        // the finalization event.

        return toResponse(review);
    }

    @Override
    public HrReviewResponse finalizeReview(
            Long appraisalId,
            Long adminId
    ) {

        validateAdmin(adminId);

        EmployeeAppraisal appraisal =
                getAppraisal(appraisalId);

        if (appraisal.getStatus() !=
                AppraisalStatus.HR_SUBMITTED) {

            throw new IllegalStateException(
                    "Appraisal cannot be finalized when status is: "
                            + appraisal.getStatus()
            );
        }

        HrReview review =
                hrReviewRepository
                        .findByAppraisalId(appraisalId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HR review not found"
                                )
                        );

        if (!"SUBMITTED".equals(
                review.getStatus()
        )) {

            throw new IllegalStateException(
                    "HR review must be submitted before finalization"
            );
        }

        review.setStatus("FINALIZED");

        appraisal.setFinalRating(
                review.getFinalRating()
        );

        appraisal.setFinalPerformanceCategory(
                review.getPerformanceCategory()
        );

        appraisal.setStatus(
                AppraisalStatus.FINALIZED
        );

        hrReviewRepository.save(review);

        EmployeeAppraisal savedAppraisal =
                employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Get Employee
        // ---------------------------------------------------------

        User employee =
                userRepository.findById(
                        savedAppraisal.getEmployeeId()
                ).orElse(null);

        // ---------------------------------------------------------
        // Get Manager
        // ---------------------------------------------------------

        User manager = null;

        if (savedAppraisal.getManagerId() != null) {

            manager =
                    userRepository.findById(
                            savedAppraisal.getManagerId()
                    ).orElse(null);
        }

        String cycleName =
                savedAppraisal.getCycle().getName();

        // ---------------------------------------------------------
        // Notify Employee
        // ---------------------------------------------------------

        if (employee != null) {

            notificationService
                    .sendTemplateNotificationAndEmail(
                            employee.getId(),
                            "APPRAISAL_FINALIZED",
                            Map.of(
                                    "employeeName",
                                    employee.getFullName(),

                                    "cycleName",
                                    cycleName
                            )
                    );
        }

        // ---------------------------------------------------------
        // Notify Manager
        // ---------------------------------------------------------

        if (manager != null) {

            notificationService
                    .sendTemplateNotificationAndEmail(
                            manager.getId(),
                            "APPRAISAL_FINALIZED_MANAGER",
                            Map.of(
                                    "employeeName",
                                    employee != null
                                            ? employee.getFullName()
                                            : "",

                                    "managerName",
                                    manager.getFullName(),

                                    "cycleName",
                                    cycleName
                            )
                    );
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

    private void validateAdmin(
            Long adminId
    ) {

        if (adminId == null) {
            throw new IllegalArgumentException(
                    "Admin ID is required"
            );
        }

        /*
         * The controller/security layer will ensure
         * that this user has ADMIN authority.
         *
         * This service also requires an authenticated
         * admin ID so the final action is auditable.
         */
    }

    private void validateHrReviewStatus(
            EmployeeAppraisal appraisal
    ) {

        if (appraisal.getStatus() !=
                AppraisalStatus.HR_REVIEW) {

            throw new IllegalStateException(
                    "HR review cannot be edited when appraisal status is: "
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
                    "Final rating must be between 1 and 5"
            );
        }
    }

    private HrReviewResponse toResponse(
            HrReview review
    ) {

        return HrReviewResponse.builder()
                .id(review.getId())
                .appraisalId(
                        review.getAppraisal() != null
                                ? review.getAppraisal().getId()
                                : null
                )
                .finalRating(
                        review.getFinalRating()
                )
                .performanceCategory(
                        review.getPerformanceCategory()
                )
                .comments(
                        review.getComments()
                )
                .status(
                        review.getStatus()
                )
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
            Long adminId
    ) {

        validateAdmin(adminId);

        if (appraisalId == null) {

            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        EmployeeAppraisal appraisal =
                getAppraisal(appraisalId);

        if (appraisal.getStatus() !=
                AppraisalStatus.MANAGER_SUBMITTED) {

            throw new IllegalStateException(
                    "HR review cannot be started when appraisal status is: "
                            + appraisal.getStatus()
            );
        }

        appraisal.setStatus(
                AppraisalStatus.HR_REVIEW
        );

        employeeAppraisalRepository.save(appraisal);

        // ---------------------------------------------------------
        // Notify HR / Admin: HR Review Started
        // ---------------------------------------------------------

        User employee =
                userRepository.findById(
                        appraisal.getEmployeeId()
                ).orElse(null);

        User admin =
                userRepository.findById(
                        adminId
                ).orElse(null);

        if (employee != null && admin != null) {

            String cycleName =
                    appraisal.getCycle().getName();

            notificationService
                    .sendTemplateNotificationAndEmail(
                            admin.getId(),
                            "APPRAISAL_HR_REVIEW_STARTED",
                            Map.of(
                                    "employeeName",
                                    employee.getFullName(),

                                    "cycleName",
                                    cycleName
                            )
                    );
        }
    }
}
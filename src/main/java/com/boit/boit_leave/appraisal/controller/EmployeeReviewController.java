package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.EmployeeReviewRequest;
import com.boit.boit_leave.appraisal.dto.EmployeeReviewResponse;
import com.boit.boit_leave.appraisal.service.EmployeeReviewService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appraisal/employee-review")
@RequiredArgsConstructor
public class EmployeeReviewController {

    private final EmployeeReviewService employeeReviewService;

    /**
     * Save employee self-review as draft.
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeReviewResponse> saveReview(
            @RequestBody EmployeeReviewRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeReviewService.saveReview(
                        request,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get employee self-review.
     */
    @GetMapping("/{appraisalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeReviewResponse> getReview(
            @PathVariable Long appraisalId
    ) {

        return ResponseEntity.ok(
                employeeReviewService.getReview(
                        appraisalId
                )
        );
    }

    /**
     * Submit employee self-review.
     */
    @PostMapping("/{appraisalId}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeReviewResponse> submitReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeReviewService.submitReview(
                        appraisalId,
                        userDetails.getUserId()
                )
        );
    }

    private CustomUserDetails getCustomUserDetails(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {

            throw new IllegalStateException(
                    "Authenticated user details are not available"
            );
        }

        return (CustomUserDetails) principal;
    }
}
package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.HrReviewRequest;
import com.boit.boit_leave.appraisal.dto.HrReviewResponse;
import com.boit.boit_leave.appraisal.service.HrReviewService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appraisal/hr-review")
@RequiredArgsConstructor
public class HrReviewController {

    private final HrReviewService hrReviewService;

    /**
     * Save HR/Admin review as draft.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HrReviewResponse> saveReview(
            @RequestBody HrReviewRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                hrReviewService.saveReview(
                        request,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get HR/Admin review.
     */
    @GetMapping("/{appraisalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HrReviewResponse> getReview(
            @PathVariable Long appraisalId
    ) {

        return ResponseEntity.ok(
                hrReviewService.getReview(
                        appraisalId
                )
        );
    }

    /**
     * Submit HR/Admin review.
     */
    @PostMapping("/{appraisalId}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HrReviewResponse> submitReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                hrReviewService.submitReview(
                        appraisalId,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Finalize appraisal.
     */
    @PostMapping("/{appraisalId}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HrReviewResponse> finalizeReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                hrReviewService.finalizeReview(
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
    
    /**
     * Start HR/Admin review.
     */
    @PostMapping("/{appraisalId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> startReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        hrReviewService.startReview(
                appraisalId,
                userDetails.getUserId()
        );

        return ResponseEntity.ok().build();
    }
}
package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.ManagerReviewRequest;
import com.boit.boit_leave.appraisal.dto.ManagerReviewResponse;
import com.boit.boit_leave.appraisal.service.ManagerReviewService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appraisal/manager-review")
@RequiredArgsConstructor
public class ManagerReviewController {

    private final ManagerReviewService managerReviewService;

    /**
     * Save manager review as draft.
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerReviewResponse> saveReview(
            @RequestBody ManagerReviewRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                managerReviewService.saveReview(
                        request,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get manager review for an appraisal.
     */
    @GetMapping("/{appraisalId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ManagerReviewResponse> getReview(
            @PathVariable Long appraisalId
    ) {

        return ResponseEntity.ok(
                managerReviewService.getReview(
                        appraisalId
                )
        );
    }

    /**
     * Submit manager review.
     */
    @PostMapping("/{appraisalId}/submit")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerReviewResponse> submitReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                managerReviewService.submitReview(
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
    
    @PostMapping("/{appraisalId}/start")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> startReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        managerReviewService.startReview(
                appraisalId,
                userDetails.getUserId()
        );

        return ResponseEntity.ok().build();
    }
}
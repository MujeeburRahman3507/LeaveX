package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.GoalProgressRequest;
import com.boit.boit_leave.appraisal.dto.GoalProgressResponse;
import com.boit.boit_leave.appraisal.service.GoalProgressService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appraisal/goals")
@RequiredArgsConstructor
public class GoalProgressController {

    private final GoalProgressService goalProgressService;

    /**
     * Update progress for a goal.
     *
     * Only the employee who owns the appraisal
     * can update the goal progress.
     */
    @PostMapping("/{goalId}/progress")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<GoalProgressResponse> updateProgress(
            @PathVariable Long goalId,
            @RequestBody GoalProgressRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                goalProgressService.updateProgress(
                        goalId,
                        request,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get the latest progress record for a goal.
     */
    @GetMapping("/{goalId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GoalProgressResponse> getLatestProgress(
            @PathVariable Long goalId
    ) {

        return ResponseEntity.ok(
                goalProgressService.getLatestProgress(
                        goalId
                )
        );
    }

    /**
     * Get complete progress history for a goal.
     */
    @GetMapping("/{goalId}/progress/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GoalProgressResponse>> getProgressHistory(
            @PathVariable Long goalId
    ) {

        return ResponseEntity.ok(
                goalProgressService.getProgressHistory(
                        goalId
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
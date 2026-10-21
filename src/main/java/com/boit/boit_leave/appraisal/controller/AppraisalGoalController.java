package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.AppraisalGoalRequest;
import com.boit.boit_leave.appraisal.dto.AppraisalGoalResponse;
import com.boit.boit_leave.appraisal.service.AppraisalGoalService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appraisal/goals")
@RequiredArgsConstructor
public class AppraisalGoalController {

    private final AppraisalGoalService appraisalGoalService;

    /**
     * Create a new goal for the logged-in employee.
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AppraisalGoalResponse> createGoal(
            @RequestBody AppraisalGoalRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        AppraisalGoalResponse response =
                appraisalGoalService.createGoal(
                        request,
                        userDetails.getUserId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Update an existing employee goal.
     */
    @PutMapping("/{goalId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AppraisalGoalResponse> updateGoal(
            @PathVariable Long goalId,
            @RequestBody AppraisalGoalRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                appraisalGoalService.updateGoal(
                        goalId,
                        request,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get a single goal.
     */
    @GetMapping("/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppraisalGoalResponse> getGoal(
            @PathVariable Long goalId
    ) {

        return ResponseEntity.ok(
                appraisalGoalService.getGoalById(
                        goalId
                )
        );
    }

    /**
     * Get all goals belonging to an appraisal.
     */
    @GetMapping("/appraisal/{appraisalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppraisalGoalResponse>> getGoalsByAppraisal(
            @PathVariable Long appraisalId
    ) {

        return ResponseEntity.ok(
                appraisalGoalService.getGoalsByAppraisal(
                        appraisalId
                )
        );
    }

    /**
     * Delete an employee goal.
     */
    @DeleteMapping("/{goalId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long goalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        appraisalGoalService.deleteGoal(
                goalId,
                userDetails.getUserId()
        );

        return ResponseEntity.noContent().build();
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
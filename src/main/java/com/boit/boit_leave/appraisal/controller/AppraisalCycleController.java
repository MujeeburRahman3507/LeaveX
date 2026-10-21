package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.AppraisalCycleRequest;
import com.boit.boit_leave.appraisal.dto.AppraisalCycleResponse;
import com.boit.boit_leave.appraisal.service.AppraisalCycleService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appraisal/cycles")
@RequiredArgsConstructor
public class AppraisalCycleController {

    private final AppraisalCycleService appraisalCycleService;

    /**
     * Create a new appraisal cycle.
     *
     * Only ADMIN can create an appraisal cycle.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppraisalCycleResponse> createCycle(
            @RequestBody AppraisalCycleRequest request,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        AppraisalCycleResponse response =
                appraisalCycleService.createCycle(
                        request,
                        userDetails.getUserId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Update an appraisal cycle.
     *
     * Only ADMIN can update the cycle.
     */
    @PutMapping("/{cycleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppraisalCycleResponse> updateCycle(
            @PathVariable Long cycleId,
            @RequestBody AppraisalCycleRequest request
    ) {

        return ResponseEntity.ok(
                appraisalCycleService.updateCycle(
                        cycleId,
                        request
                )
        );
    }

    /**
     * Get an appraisal cycle by ID.
     *
     * All authenticated users can view the cycle.
     */
    @GetMapping("/{cycleId}")
    public ResponseEntity<AppraisalCycleResponse> getCycle(
            @PathVariable Long cycleId
    ) {

        return ResponseEntity.ok(
                appraisalCycleService.getCycleById(
                        cycleId
                )
        );
    }

    /**
     * Get all appraisal cycles.
     *
     * All authenticated users can view cycles.
     */
    @GetMapping
    public ResponseEntity<List<AppraisalCycleResponse>> getAllCycles() {

        return ResponseEntity.ok(
                appraisalCycleService.getAllCycles()
        );
    }

    /**
     * Update appraisal cycle status.
     *
     * Only ADMIN can change the cycle status.
     */
    @PatchMapping("/{cycleId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppraisalCycleResponse> updateCycleStatus(
            @PathVariable Long cycleId,
            @RequestParam String status
    ) {

        return ResponseEntity.ok(
                appraisalCycleService.updateCycleStatus(
                        cycleId,
                        status
                )
        );
    }

    /**
     * Extract our application's CustomUserDetails
     * from Spring Security authentication.
     */
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
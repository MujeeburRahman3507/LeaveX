package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.EmployeeAppraisalResponse;
import com.boit.boit_leave.appraisal.service.EmployeeAppraisalService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/appraisal")
@RequiredArgsConstructor
public class EmployeeAppraisalController {

    private final EmployeeAppraisalService employeeAppraisalService;
    
    private final UserRepository userRepository;

    /**
     * Create an appraisal for the logged-in employee.
     *
     * ADMIN can also create an appraisal for an employee
     * by supplying employeeId.
     */
    @PostMapping("/cycles/{cycleId}/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<EmployeeAppraisalResponse> createAppraisal(
            @PathVariable Long cycleId,
            @PathVariable Long employeeId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        /*
         * Employee can only create their own appraisal.
         * ADMIN can create an appraisal for another employee.
         */
        if (hasRole(authentication, "ROLE_EMPLOYEE") &&
                !employeeId.equals(userDetails.getUserId())) {

            throw new IllegalStateException(
                    "Employee can only create their own appraisal"
            );
        }

        EmployeeAppraisalResponse response =
                employeeAppraisalService.createAppraisal(
                        cycleId,
                        employeeId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get appraisal by ID.
     */
    @GetMapping("/{appraisalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeAppraisalResponse> getAppraisal(
            @PathVariable Long appraisalId
    ) {

        return ResponseEntity.ok(
                employeeAppraisalService.getById(
                        appraisalId
                )
        );
    }

    /**
     * Get the logged-in employee's appraisal
     * for a particular cycle.
     */
    @GetMapping("/cycles/{cycleId}/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeAppraisalResponse> getMyAppraisal(
            @PathVariable Long cycleId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeAppraisalService.getEmployeeAppraisal(
                        cycleId,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get all appraisals belonging to the logged-in employee.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<EmployeeAppraisalResponse>> getMyAppraisals(
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeAppraisalService.getEmployeeAppraisals(
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get manager's team appraisals.
     */
    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<EmployeeAppraisalResponse>> getTeamAppraisals(
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeAppraisalService.getManagerAppraisals(
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Get all appraisals.
     *
     * ADMIN only.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeAppraisalResponse>> getAllAppraisals() {

        return ResponseEntity.ok(
                employeeAppraisalService.getAllAppraisals()
        );
    }

    /**
     * Employee submits their goals for manager review.
     */
    @PostMapping("/{appraisalId}/submit-goals")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeAppraisalResponse> submitGoals(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeAppraisalService.submitGoals(
                        appraisalId,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Manager approves employee goals.
     */
    @PostMapping("/{appraisalId}/approve-goals")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeAppraisalResponse> approveGoals(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeAppraisalService.approveGoals(
                        appraisalId,
                        userDetails.getUserId()
                )
        );
    }

    /**
     * Manager requests changes to employee goals.
     */
    @PostMapping("/{appraisalId}/request-goal-revision")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeAppraisalResponse> requestGoalRevision(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        return ResponseEntity.ok(
                employeeAppraisalService.requestGoalRevision(
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

    private boolean hasRole(
            Authentication authentication,
            String role
    ) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals(role)
                );
    }
    
    @PostMapping("/{appraisalId}/start-self-review")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> startSelfReview(
            @PathVariable Long appraisalId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Logged-in user not found"
                        )
                );

        employeeAppraisalService.startSelfReview(
                appraisalId,
                user.getId()
        );

        return ResponseEntity.ok().build();
    }
}
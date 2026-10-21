package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.*;
import com.boit.boit_leave.service.AuditService;
import com.boit.boit_leave.service.LeavePolicyService;
import com.boit.boit_leave.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import com.boit.boit_leave.entity.LeaveStatus;
import com.boit.boit_leave.dto.WorkflowActionDto;
import com.boit.boit_leave.dto.LeaveResponseDto;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.LeaveService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    
    private final AuditService auditService;
    
    private final LeavePolicyService
    leavePolicyService;
    
    private final LeaveService leaveService;

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminStatisticsDto>>
    statistics() {

        return ResponseEntity.ok(
                ApiResponse.<AdminStatisticsDto>builder()
                        .success(true)
                        .message("Statistics loaded")
                        .data(
                                userService.getAdminStatistics())
                        .build()
        );
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<Object>>
    employees() {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Employees loaded")
                        .data(
                                userService.getAllEmployees())
                        .build()
        );
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>>
    employee(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponseDto>builder()
                        .success(true)
                        .message("Employee loaded")
                        .data(
                                userService.getEmployeeById(id))
                        .build()
        );
    }

    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>>
    create(
            @Valid
            @RequestBody
            EmployeeCreateDto request) {

        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponseDto>builder()
                        .success(true)
                        .message("Employee created")
                        .data(
                                userService.createEmployee(
                                        request))
                        .build()
        );
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>>
    update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            EmployeeUpdateDto request) {

        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponseDto>builder()
                        .success(true)
                        .message("Employee updated")
                        .data(
                                userService.updateEmployee(
                                        id,
                                        request))
                        .build()
        );
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ApiResponse<Object>>
    delete(
            @PathVariable Long id) {

        userService.deleteEmployee(id);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Employee deleted")
                        .build()
        );
    }

    @PutMapping("/employees/{id}/balance")
    public ResponseEntity<ApiResponse<EmployeeBalanceDto>>
    updateBalance(
            @PathVariable Long id,
            @Valid
            @RequestBody
            LeaveBalanceUpdateDto request) {

        return ResponseEntity.ok(
                ApiResponse.<EmployeeBalanceDto>builder()
                        .success(true)
                        .message("Balance updated")
                        .data(
                                userService.updateLeaveBalance(
                                        id,
                                        request))
                        .build()
        );
    }
    
    @GetMapping("/managers")
    public ResponseEntity<?> managers() {

        return ResponseEntity.ok(
                userService.getManagers()
        );
    }

    @GetMapping("/admins")
    public ResponseEntity<?> admins() {

        return ResponseEntity.ok(
                userService.getAdmins()
        );
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Object>>
    auditLogs() {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Audit logs loaded")
                        .data(
                                auditService.getAllLogs()
                        )
                        .build()
        );
    }
    
    @GetMapping("/leave-policy")
    public ResponseEntity<?> getPolicies() {

        return ResponseEntity.ok(
                leavePolicyService
                        .getAllPolicies()
        );
    }
    
    @PostMapping("/leave-policy")
    public ResponseEntity<?> createPolicy(
            @RequestBody
            LeavePolicyDto dto) {

        return ResponseEntity.ok(
                leavePolicyService
                        .createPolicy(dto)
        );
    }
    
    @PutMapping("/leave-policy/{id}")
    public ResponseEntity<?> updatePolicy(
            @PathVariable Long id,
            @RequestBody LeavePolicyDto dto) {

        return ResponseEntity.ok(
                leavePolicyService
                        .updatePolicy(
                                id,
                                dto)
        );
    }
    
    @DeleteMapping("/leave-policy/{id}")
    public ResponseEntity<?> deletePolicy(
            @PathVariable Long id) {

        leavePolicyService
                .deletePolicy(id);

        return ResponseEntity.ok(
                "Policy Deleted Successfully"
        );
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>>
    dashboard() {

        return ResponseEntity.ok(

                ApiResponse
                        .<AdminDashboardDto>builder()
                        .success(true)
                        .message(
                                "Dashboard Loaded"
                        )
                        .data(
                                userService
                                        .getAdminDashboard()
                        )
                        .build()
        );
    }
    
    @GetMapping("/country-stats")
    public ResponseEntity<ApiResponse<List<CountryEmployeeCountDto>>>
    getCountryStats() {

        return ResponseEntity.ok(

            ApiResponse.<List<CountryEmployeeCountDto>>builder()
                    .success(true)
                    .message("Country statistics loaded")
                    .data(
                        userService
                            .getEmployeeCountByCountry()
                    )
                    .build()

        );
    }
    
 // =========================================================
 // ADMIN APPROVALS
 // =========================================================

 @GetMapping("/approvals")
 public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> pendingApprovals(
         @AuthenticationPrincipal CustomUserDetails user) {

     List<LeaveResponseDto> requests =
             leaveService.getManagerPendingRequests(
                     user.getUserId()
             );

     return ResponseEntity.ok(
             ApiResponse.<List<LeaveResponseDto>>builder()
                     .success(true)
                     .message("Admin approval requests loaded")
                     .data(requests)
                     .build()
     );
 }


 // =========================================================
 // ADMIN APPROVE LEAVE
 // =========================================================

 @PostMapping("/approvals/approve")
 public ResponseEntity<ApiResponse<Object>> approveLeave(
         @Valid
         @RequestBody
         WorkflowActionDto request,

         @AuthenticationPrincipal
         CustomUserDetails user) {

     return ResponseEntity.ok(
             ApiResponse.builder()
                     .success(true)
                     .message("Leave approved")
                     .data(
                             leaveService.processLeaveAction(
                                     request.getRequestId(),
                                     LeaveStatus.APPROVED,
                                     user.getUserId(),
                                     request.getManagerComments()
                             )
                     )
                     .build()
     );
 }


 // =========================================================
 // ADMIN REJECT LEAVE
 // =========================================================

 @PostMapping("/approvals/reject")
 public ResponseEntity<ApiResponse<Object>> rejectLeave(
         @Valid
         @RequestBody
         WorkflowActionDto request,

         @AuthenticationPrincipal
         CustomUserDetails user) {

     return ResponseEntity.ok(
             ApiResponse.builder()
                     .success(true)
                     .message("Leave rejected")
                     .data(
                             leaveService.processLeaveAction(
                                     request.getRequestId(),
                                     LeaveStatus.REJECTED,
                                     user.getUserId(),
                                     request.getManagerComments()
                             )
                     )
                     .build()
     );
 }
}
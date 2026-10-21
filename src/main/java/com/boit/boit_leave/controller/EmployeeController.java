package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.*;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.HolidayService;
import com.boit.boit_leave.service.LeaveCalculationService;
import com.boit.boit_leave.service.LeaveService;
import com.boit.boit_leave.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.boit.boit_leave.entity.LeaveDurationType;
import java.time.LocalDate;
import java.util.List;
import com.boit.boit_leave.dto.BackupPersonDto;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    private final LeaveService leaveService;
    private final UserService userService;
    private final HolidayService holidayService;
    private final LeaveCalculationService leaveCalculationService;
    

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetricsDto>>
    dashboard(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        DashboardMetricsDto metrics =
                userService.getDashboardMetrics(
                        user.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<DashboardMetricsDto>builder()
                        .success(true)
                        .message("Dashboard loaded")
                        .data(metrics)
                        .build()
        );
    }

    @PostMapping("/leave/apply")
    public ResponseEntity<ApiResponse<LeaveResponseDto>>
    applyLeave(
            @Valid
            @RequestBody
            LeaveRequestDto request,
            @AuthenticationPrincipal
            CustomUserDetails user) {

        LeaveResponseDto response =
                leaveService.applyLeave(
                        request,
                        user.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<LeaveResponseDto>builder()
                        .success(true)
                        .message("Leave submitted successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/leave/history")
    public ResponseEntity<ApiResponse<Object>>
    history(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Leave history")
                        .data(
                                leaveService.getMyRequests(
                                        user.getUserId()))
                        .build()
        );
    }
    
    @PutMapping("/leave/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>>
    cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal
            CustomUserDetails user) {

        leaveService.cancelLeave(
                id,
                user.getUserId()
        );

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Leave Cancelled Successfully")
                        .build()
        );
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<EmployeeBalanceDto>>
    balance(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(
                ApiResponse.<EmployeeBalanceDto>builder()
                        .success(true)
                        .message("Balance details")
                        .data(
                                userService.getEmployeeBalance(
                                        user.getUserId()))
                        .build()
        );
    }
    
    @GetMapping("/upcoming-holidays")
    public ResponseEntity<ApiResponse<List<HolidayDto>>>
    upcomingHolidays(

            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(

                ApiResponse
                        .<List<HolidayDto>>builder()

                        .success(true)

                        .message(
                                "Upcoming Holidays")

                        .data(

                                holidayService
                                        .getEmployeeHolidays(
                                                user.getUserId())
                        )

                        .build()
        );
    }
    
    @GetMapping("/calculate-days")
    public ResponseEntity<?> calculateDays(

            @RequestParam LocalDate startDate,

            @RequestParam LocalDate endDate,

            @AuthenticationPrincipal
            CustomUserDetails user) {

        LeaveCalculationResponseDto response =
                leaveCalculationService
                        .getLeaveCalculationDetails(
                                user.getUserId(),
                                startDate,
                                endDate,
                                LeaveDurationType.FULL_DAY
                        );

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/birthdays/today")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BirthdayDto>>> getTodaysBirthdays() {

        return ResponseEntity.ok(

                ApiResponse.<List<BirthdayDto>>builder()

                        .success(true)

                        .message("Today's Birthdays")

                        .data(
                                userService.getTodaysBirthdays()
                        )

                        .build()

        );
    }
    
    @GetMapping("/birthday")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BirthdayGreetingDto>>
    getBirthdayGreeting(

            @AuthenticationPrincipal
            CustomUserDetails user

    ) {

        BirthdayGreetingDto response =

                userService.getBirthdayGreeting(

                        user.getUserId()

                );

        return ResponseEntity.ok(

                ApiResponse
                        .<BirthdayGreetingDto>builder()

                        .success(true)

                        .message("Birthday Greeting")

                        .data(response)

                        .build()

        );

    }
    
    @GetMapping("/leave/backup-persons")
    public ResponseEntity<ApiResponse<List<BackupPersonDto>>> getBackupPersons(
            @AuthenticationPrincipal CustomUserDetails user) {

        List<BackupPersonDto> backupPersons =
                leaveService.getBackupPersons(
                        user.getUserId()
                );

        return ResponseEntity.ok(
                ApiResponse.<List<BackupPersonDto>>builder()
                        .success(true)
                        .message("Backup persons retrieved successfully")
                        .data(backupPersons)
                        .build()
        );
    }
    
    
}
package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.ApiResponse;
import com.boit.boit_leave.dto.BackupPersonDto;
import com.boit.boit_leave.dto.DashboardMetricsDto;
import com.boit.boit_leave.dto.LeaveRequestDto;
import com.boit.boit_leave.dto.LeaveResponseDto;
import com.boit.boit_leave.dto.WorkflowActionDto;
import com.boit.boit_leave.entity.LeaveStatus;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.LeaveService;
import com.boit.boit_leave.service.UserService;
import com.boit.boit_leave.dto.BirthdayGreetingDto;
import jakarta.validation.Valid;
import com.boit.boit_leave.dto.HolidayDto;
import com.boit.boit_leave.service.HolidayService;
import lombok.RequiredArgsConstructor;
import com.boit.boit_leave.dto.LeaveCalculationResponseDto;
import com.boit.boit_leave.entity.LeaveDurationType;
import com.boit.boit_leave.service.LeaveCalculationService;
import java.time.LocalDate;
import java.util.List;
import com.boit.boit_leave.dto.BackupPersonDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final LeaveService leaveService;

    private final UserService userService;

    private final HolidayService holidayService;
    
    private final LeaveCalculationService leaveCalculationService;
    // =========================================================
    // MANAGER APPROVALS
    // =========================================================

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>>
    pending(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        List<LeaveResponseDto> requests =
                leaveService.getManagerPendingRequests(
                        user.getUserId()
                );

        return ResponseEntity.ok(

                ApiResponse.<List<LeaveResponseDto>>builder()

                        .success(true)

                        .message(
                                "Pending requests loaded"
                        )

                        .data(requests)

                        .build()
        );
    }


    // =========================================================
    // APPROVE LEAVE
    // =========================================================

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<Object>>
    approve(

            @Valid
            @RequestBody
            WorkflowActionDto request,

            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(

                ApiResponse.builder()

                        .success(true)

                        .message(
                                "Leave approved"
                        )

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
    // REJECT LEAVE
    // =========================================================

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Object>>
    reject(

            @Valid
            @RequestBody
            WorkflowActionDto request,

            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(

                ApiResponse.builder()

                        .success(true)

                        .message(
                                "Leave rejected"
                        )

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


    // =========================================================
    // EMPLOYEES ON LEAVE
    // =========================================================

    @GetMapping("/employees-on-leave")
    public ResponseEntity<?> employeesOnLeave() {

        return ResponseEntity.ok(

                leaveService
                        .getEmployeesOnLeaveToday()

        );
    }


    // =========================================================
    // MANAGER TEAM CALENDAR
    // =========================================================

    @GetMapping("/calendar")
    public ResponseEntity<?> calendar() {

        return ResponseEntity.ok(

                leaveService
                        .getManagerCalendar()

        );
    }


    // =========================================================
    // MANAGER PERSONAL LEAVE SUMMARY
    // =========================================================
    //
    // Used by:
    // Manager -> My Leave Requests
    //
    // Returns the manager's own:
    // Annual Balance
    // Used Leave
    // Remaining Leave
    // Approved
    // Rejected
    // Pending
    //
    // =========================================================

    @GetMapping("/leave/summary")
    public ResponseEntity<ApiResponse<DashboardMetricsDto>>
    myLeaveSummary(

            @AuthenticationPrincipal
            CustomUserDetails user) {

        DashboardMetricsDto metrics =
                userService.getDashboardMetrics(
                        user.getUserId()
                );

        return ResponseEntity.ok(

                ApiResponse.<DashboardMetricsDto>builder()

                        .success(true)

                        .message(
                                "Leave summary loaded"
                        )

                        .data(metrics)

                        .build()
        );
    }


    // =========================================================
    // MANAGER APPLY LEAVE
    // =========================================================
    //
    // Manager uses the same existing LeaveService logic
    // as Employee.
    //
    // The logged-in manager's userId is passed so the leave
    // belongs to the manager.
    //
    // =========================================================

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

                        user.getUserId()

                );

        return ResponseEntity.ok(

                ApiResponse.<LeaveResponseDto>builder()

                        .success(true)

                        .message(
                                "Leave submitted successfully"
                        )

                        .data(response)

                        .build()
        );
    }


    // =========================================================
    // MANAGER MY LEAVE REQUESTS
    // =========================================================
    //
    // Returns only the logged-in manager's own leave requests.
    //
    // =========================================================

    @GetMapping("/leave/history")
    public ResponseEntity<ApiResponse<Object>>
    myLeaveHistory(

            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(

                ApiResponse.builder()

                        .success(true)

                        .message(
                                "Leave history"
                        )

                        .data(

                                leaveService.getMyRequests(

                                        user.getUserId()

                                )

                        )

                        .build()
        );
    }


    // =========================================================
    // MANAGER CANCEL OWN LEAVE
    // =========================================================
    //
    // Manager can cancel their own pending leave request.
    // Existing LeaveService performs the actual validation.
    //
    // =========================================================

    @PutMapping("/leave/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>>
    cancelMyLeave(

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

                        .message(
                                "Leave Cancelled Successfully"
                        )

                        .build()
        );
    }
    
 // =========================================================
 // MANAGER BIRTHDAY
 // =========================================================
 // Used by:
 // Manager -> My Leave Requests
 //
 // Returns the logged-in manager's birthday information.
 // =========================================================

 @GetMapping("/leave/birthday")
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

                     .message(
                             "Birthday Greeting"
                     )

                     .data(response)

                     .build()
     );
 }
 
 @GetMapping("/leave/upcoming-holidays")
 public ResponseEntity<ApiResponse<List<HolidayDto>>>
 upcomingHolidays(
         @AuthenticationPrincipal
         CustomUserDetails user) {

     return ResponseEntity.ok(

             ApiResponse
                     .<List<HolidayDto>>builder()

                     .success(true)

                     .message(
                             "Upcoming Holidays"
                     )

                     .data(
                             holidayService
                                     .getEmployeeHolidays(
                                             user.getUserId()
                                     )
                     )

                     .build()
     );
 }
 
 @GetMapping("/leave/calculate-days")
 public ResponseEntity<?> calculateDays(

         @RequestParam LocalDate startDate,

         @RequestParam LocalDate endDate,

         @RequestParam LeaveDurationType durationType,

         @AuthenticationPrincipal
         CustomUserDetails user) {

     LeaveCalculationResponseDto response =
             leaveCalculationService
                     .getLeaveCalculationDetails(
                             user.getUserId(),
                             startDate,
                             endDate,
                             durationType
                     );

     return ResponseEntity.ok(response);
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
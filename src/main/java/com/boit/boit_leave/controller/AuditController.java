package com.boit.boit_leave.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.boit.boit_leave.dto.ApiResponse;
import com.boit.boit_leave.dto.LeaveTimelineDto;
import com.boit.boit_leave.service.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/leave/{leaveRequestId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<LeaveTimelineDto>>> getLeaveTimeline(

            @PathVariable
            Long leaveRequestId

    ) {

        return ResponseEntity.ok(

                ApiResponse.<List<LeaveTimelineDto>>builder()

                        .success(true)

                        .message("Leave timeline loaded successfully.")

                        .data(

                                auditService.getLeaveTimeline(
                                        leaveRequestId
                                )

                        )

                        .build()

        );

    }

}
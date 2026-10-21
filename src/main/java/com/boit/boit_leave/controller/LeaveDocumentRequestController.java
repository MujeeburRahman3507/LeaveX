package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.ApiResponse;
import com.boit.boit_leave.dto.CreateDocumentRequestDto;
import com.boit.boit_leave.dto.DocumentRequestDto;
import com.boit.boit_leave.dto.EmployeeActionDto;
import com.boit.boit_leave.dto.ManagerActionDto;
import com.boit.boit_leave.dto.RejectDocumentDto;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.LeaveDocumentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave/document-request")
@RequiredArgsConstructor
public class LeaveDocumentRequestController {

    private final LeaveDocumentRequestService
            leaveDocumentRequestService;

    /*
     * Manager requests additional documents
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentRequestDto>>
    requestDocument(

            @Valid
            @RequestBody
            CreateDocumentRequestDto dto,

            @AuthenticationPrincipal
            CustomUserDetails user

    ) {

        DocumentRequestDto response =

                leaveDocumentRequestService

                        .requestDocument(

                                user.getUserId(),

                                dto

                        );

        return ResponseEntity.ok(

                ApiResponse.<DocumentRequestDto>builder()

                        .success(true)

                        .message("Document request created successfully.")

                        .data(response)

                        .build()

        );

    }

    /*
     * Complete request history
     */
    @GetMapping("/{leaveRequestId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<DocumentRequestDto>>>
    getRequests(

            @PathVariable
            Long leaveRequestId

    ) {

        return ResponseEntity.ok(

                ApiResponse.<List<DocumentRequestDto>>builder()

                        .success(true)

                        .message("Document requests loaded.")

                        .data(

                                leaveDocumentRequestService

                                        .getDocumentRequests(

                                                leaveRequestId

                                        )

                        )

                        .build()

        );

    }

    /*
     * Pending requests only
     */
    @GetMapping("/pending/{leaveRequestId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<DocumentRequestDto>>>
    getPendingRequests(

            @PathVariable
            Long leaveRequestId

    ) {

        return ResponseEntity.ok(

                ApiResponse.<List<DocumentRequestDto>>builder()

                        .success(true)

                        .message("Pending requests loaded.")

                        .data(

                                leaveDocumentRequestService

                                        .getPendingRequests(

                                                leaveRequestId

                                        )

                        )

                        .build()

        );

    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<DocumentRequestDto>>> getPendingRequestsByEmployee(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ResponseEntity.ok(

                ApiResponse
                        .<List<DocumentRequestDto>>builder()
                        .success(true)
                        .message("Pending document requests loaded")
                        .data(
                                leaveDocumentRequestService
                                        .getPendingRequestsByEmployee(
                                                user.getUserId()
                                        )
                        )
                        .build()

        );

    }
    
    @PutMapping("/verify/{requestId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyDocument(

            @PathVariable
            Long requestId

    ) {

        leaveDocumentRequestService.verifyDocument(
                requestId
        );

        return ResponseEntity.ok(

                ApiResponse.<Void>builder()

                        .success(true)

                        .message("Document verified successfully.")

                        .build()

        );

    }
    
    @PutMapping("/reject/{requestId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectDocument(

            @PathVariable
            Long requestId,
            
            @RequestBody
            @Valid
            RejectDocumentDto dto

    ) {

        leaveDocumentRequestService.rejectDocument(
                requestId, dto
        );

        return ResponseEntity.ok(

                ApiResponse.<Void>builder()

                        .success(true)

                        .message("Document rejected.")

                        .build()

        );

    }
    
    @GetMapping("/employee-actions")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<EmployeeActionDto>>> getEmployeeActions(

            @AuthenticationPrincipal
            CustomUserDetails user

    ){

        return ResponseEntity.ok(

                ApiResponse

                        .<List<EmployeeActionDto>>builder()

                        .success(true)

                        .message("Employee actions loaded")

                        .data(

                                leaveDocumentRequestService

                                        .getEmployeeActions(

                                                user.getUserId()

                                        )

                        )

                        .build()

        );

    }
    
    @GetMapping("/manager-pending-actions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeActionDto>>> getManagerPendingActions(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ResponseEntity.ok(
                ApiResponse
                        .<List<EmployeeActionDto>>builder()
                        .success(true)
                        .message("Manager pending document actions loaded")
                        .data(
                                leaveDocumentRequestService
                                        .getManagerPendingActions(
                                                user.getUserId()
                                        )
                        )
                        .build()
        );
    }
    
    @GetMapping("/manager-actions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ManagerActionDto>>>

    getManagerActions(

            @AuthenticationPrincipal
            CustomUserDetails user

    ){

        return ResponseEntity.ok(

                ApiResponse

                        .<List<ManagerActionDto>>builder()

                        .success(true)

                        .message("Manager actions loaded")

                        .data(

                                leaveDocumentRequestService

                                        .getManagerActions(

                                                user.getUserId()

                                        )

                        )

                        .build()

        );

    }
    
    @GetMapping("/admin-actions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ManagerActionDto>>> getAdminActions(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ResponseEntity.ok(
                ApiResponse
                        .<List<ManagerActionDto>>builder()
                        .success(true)
                        .message("Admin document actions loaded")
                        .data(
                                leaveDocumentRequestService
                                        .getAdminActions(
                                                user.getUserId()
                                        )
                        )
                        .build()
        );
    }

}
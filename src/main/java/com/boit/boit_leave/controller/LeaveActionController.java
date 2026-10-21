package com.boit.boit_leave.controller;

import com.boit.boit_leave.entity.LeaveActionToken;
import com.boit.boit_leave.entity.LeaveActionType;
import com.boit.boit_leave.entity.LeaveStatus;
import com.boit.boit_leave.service.LeaveActionTokenService;
import com.boit.boit_leave.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boit.boit_leave.dto.CreateDocumentRequestDto;
import com.boit.boit_leave.entity.DocumentType;
import com.boit.boit_leave.entity.LeaveActionToken;
import com.boit.boit_leave.entity.LeaveActionType;
import com.boit.boit_leave.service.LeaveDocumentRequestService;

import java.util.Map;

@RestController
@RequestMapping("/api/leave-action")
@RequiredArgsConstructor
public class LeaveActionController {

    private final LeaveActionTokenService leaveActionTokenService;
    private final LeaveService leaveService;
    private final LeaveDocumentRequestService leaveDocumentRequestService;
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestParam String token) {

        try {
            LeaveActionToken actionToken =
                    leaveActionTokenService.validateToken(token);

            return ResponseEntity.ok(
                    Map.of(
                            "valid", true,
                            "action", actionToken.getAction().name(),
                            "leaveRequestId",
                            actionToken.getLeaveRequest().getId(),
                            "managerId",
                            actionToken.getManager().getId(),
                            "employeeName",
                            actionToken.getLeaveRequest()
                                    .getUser()
                                    .getFullName()
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "valid", false,
                            "message", ex.getMessage()
                    )
            );
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approve(
            @RequestParam String token) {

        try {

            LeaveActionToken actionToken =
                    leaveActionTokenService.validateToken(token);

            if (actionToken.getAction()
                    != LeaveActionType.APPROVE) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "Invalid token for APPROVE action"
                        )
                );
            }

            Long requestId =
                    actionToken.getLeaveRequest().getId();

            Long managerId =
                    actionToken.getManager().getId();

            leaveService.processLeaveAction(
                    requestId,
                    LeaveStatus.APPROVED,
                    managerId,
                    "Approved through email action"
            );

            leaveActionTokenService.markAsUsed(actionToken);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message",
                            "Leave request approved successfully"
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    )
            );
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<?> reject(
            @RequestParam String token) {

        try {

            LeaveActionToken actionToken =
                    leaveActionTokenService.validateToken(token);

            if (actionToken.getAction()
                    != LeaveActionType.REJECT) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "Invalid token for REJECT action"
                        )
                );
            }

            Long requestId =
                    actionToken.getLeaveRequest().getId();

            Long managerId =
                    actionToken.getManager().getId();

            leaveService.processLeaveAction(
                    requestId,
                    LeaveStatus.REJECTED,
                    managerId,
                    "Rejected through email action"
            );

            leaveActionTokenService.markAsUsed(actionToken);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message",
                            "Leave request rejected successfully"
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    )
            );
        }
    }

    @PostMapping("/request-document")
    public ResponseEntity<?> requestDocument(
            @RequestParam String token,
            @RequestBody CreateDocumentRequestDto dto) {

        try {

            LeaveActionToken actionToken =
                    leaveActionTokenService.validateToken(token);

            if (actionToken.getAction()
                    != LeaveActionType.REQUEST_DOCUMENT) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "Invalid token for document request action"
                        )
                );
            }

            if (!actionToken.getLeaveRequest()
                    .getId()
                    .equals(dto.getLeaveRequestId())) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "Leave request does not match the action token"
                        )
                );
            }

            Long managerId =
                    actionToken.getManager().getId();

            leaveDocumentRequestService.requestDocument(
                    managerId,
                    dto
            );

            leaveActionTokenService.markAsUsed(actionToken);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message",
                            "Supporting document request sent successfully"
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    )
            );
        }
    }
}
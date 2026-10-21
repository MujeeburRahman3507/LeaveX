package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.AuditLogDto;
import com.boit.boit_leave.dto.LeaveTimelineDto;
import com.boit.boit_leave.entity.AuditLog;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(
            String actionType,
            User actionBy,
            String entityName,
            Long entityId,
            String actionDetails) {

        try {

            AuditLog auditLog = AuditLog.builder()
                    .actionType(actionType)
                    .actionBy(actionBy)
                    .entityName(entityName)
                    .entityId(entityId)
                    .actionDetails(actionDetails)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.info(
                    "Audit Log Saved -> Action={}, Entity={}, EntityId={}",
                    actionType,
                    entityName,
                    entityId
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to write audit log : {}",
                    ex.getMessage(),
                    ex
            );
        }
    }
    
    public List<AuditLogDto> getAllLogs() {

    	return auditLogRepository
    	        .findAllByOrderByCreatedAtDesc()
    	        .stream()
    	        .map(log ->
    	                AuditLogDto.builder()
    	                        .id(log.getId())
    	                        .actionType(log.getActionType())
    	                        .actionBy(
    	                                log.getActionBy() != null
    	                                        ? log.getActionBy().getFullName()
    	                                        : "System"
    	                        )
    	                        .entityName(log.getEntityName())
    	                        .entityId(log.getEntityId())
    	                        .actionDetails(log.getActionDetails())
    	                        .createdAt(log.getCreatedAt())
    	                        .build()
    	        )
    	        .toList();
    }
    
    @Transactional(readOnly = true)
    public List<LeaveTimelineDto> getLeaveTimeline(
            Long leaveRequestId
    ) {

        return auditLogRepository

                .findByEntityNameAndEntityIdOrderByCreatedAtAsc(

                        "LEAVE_REQUEST",

                        leaveRequestId

                )

                .stream()

                .map(log -> LeaveTimelineDto.builder()

                        .action(
                                formatAction(
                                        log.getActionType()
                                )
                        )

                        .performedBy(

                                log.getActionBy() != null

                                        ? log.getActionBy().getFullName()

                                        : "System"

                        )

                        .comments(
                                log.getActionDetails()
                        )

                        .actionDate(
                                log.getCreatedAt()
                        )

                        .icon(
                                getIcon(
                                        log.getActionType()
                                )
                        )

                        .color(
                                getColor(
                                        log.getActionType()
                                )
                        )

                        .build()

                )

                .toList();

    }
    
    private String formatAction(
            String action
    ) {

        return switch (action) {

            case "LEAVE_APPLIED" ->
                    "Leave Applied";

            case "LEAVE_APPROVED" ->
                    "Leave Approved";

            case "LEAVE_REJECTED" ->
                    "Leave Rejected";

            case "DOCUMENT_REQUESTED" ->
                    "Document Requested";

            case "DOCUMENT_UPLOADED" ->
                    "Document Uploaded";

            case "DOCUMENT_VERIFIED" ->
                    "Document Verified";

            case "DOCUMENT_REJECTED" ->
                    "Document Rejected";

            case "LEAVE_CANCELLED" ->
                    "Leave Cancelled";

            default ->
                    action.replace("_", " ");

        };

    }
    
    private String getIcon(
            String action
    ) {

        return switch (action) {

            case "LEAVE_APPLIED" ->
                    "FILE_PLUS";

            case "DOCUMENT_REQUESTED" ->
                    "FILE_WARNING";

            case "DOCUMENT_UPLOADED" ->
                    "UPLOAD";

            case "DOCUMENT_VERIFIED" ->
                    "CHECK";

            case "DOCUMENT_REJECTED" ->
                    "X";

            case "LEAVE_APPROVED" ->
                    "CHECK_CIRCLE";

            case "LEAVE_REJECTED" ->
                    "X_CIRCLE";

            default ->
                    "INFO";

        };

    }
    
    private String getColor(
            String action
    ) {

        return switch (action) {

            case "LEAVE_APPLIED" ->
                    "blue";

            case "DOCUMENT_REQUESTED" ->
                    "orange";

            case "DOCUMENT_UPLOADED" ->
                    "indigo";

            case "DOCUMENT_VERIFIED" ->
                    "green";

            case "DOCUMENT_REJECTED" ->
                    "red";

            case "LEAVE_APPROVED" ->
                    "green";

            case "LEAVE_REJECTED" ->
                    "red";

            default ->
                    "gray";

        };

    }
}

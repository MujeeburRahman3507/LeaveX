package com.boit.boit_leave.service;

import java.time.LocalDateTime;
import com.boit.boit_leave.dto.ManagerActionDto;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boit.boit_leave.dto.CreateDocumentRequestDto;
import com.boit.boit_leave.dto.DocumentRequestDto;
import com.boit.boit_leave.dto.EmployeeActionDto;
import com.boit.boit_leave.dto.RejectDocumentDto;
import com.boit.boit_leave.entity.DocumentRequestStatus;
import com.boit.boit_leave.entity.DocumentStatus;
import com.boit.boit_leave.entity.DocumentType;
import com.boit.boit_leave.entity.LeaveDocumentRequest;
import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.exception.ResourceNotFoundException;
import com.boit.boit_leave.repository.LeaveDocumentRequestRepository;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.boit.boit_leave.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveDocumentRequestService {

    private final LeaveDocumentRequestRepository
            leaveDocumentRequestRepository;

    private final LeaveRequestRepository
            leaveRequestRepository;

    private final UserRepository
            userRepository;

    private final NotificationService
            notificationService;

    private final EmailService
            emailService;

    private final AuditService
            auditService;
    
    public DocumentRequestDto requestDocument(

            Long managerId,

            CreateDocumentRequestDto dto

    ) {

        LeaveRequest leaveRequest =

                leaveRequestRepository

                        .findById(

                                dto.getLeaveRequestId()

                        )

                        .orElseThrow(() ->

                                new ResourceNotFoundException(

                                        "Leave request not found"

                                )

                        );

        User manager =

                userRepository

                        .findById(

                                managerId

                        )

                        .orElseThrow(() ->

                                new ResourceNotFoundException(

                                        "Manager not found"

                                )

                        );

        /*
         * Validation
         */

        validateManager(

                leaveRequest,

                manager

        );

        validateDuplicateRequest(

                leaveRequest.getId(),

                dto

        );

        LeaveDocumentRequest request =

                LeaveDocumentRequest

                        .builder()

                        .leaveRequest(

                                leaveRequest

                        )

                        .requestedBy(

                                manager

                        )

                        .documentType(

                                dto.getDocumentType()

                        )

                        .comments(

                                dto.getComments()

                        )
                        .status(
                                DocumentRequestStatus.PENDING
                        )

                        .documentStatus(
                                DocumentStatus.REQUESTED
                        )

                        .priority(
                                dto.getPriority()
                        )

                        .requestedAt(
                                LocalDateTime.now()
                        )

                        .build();

        request =

                leaveDocumentRequestRepository

                        .save(

                                request

                        );

        /*
         * Notification
         */

        notificationService.createNotification(

                leaveRequest

                        .getUser()

                        .getId(),

                "Documents Requested",

                "Manager requested "

                        + dto.getDocumentType()

                                .name()

                                .replace("_", " ")

        );

        /*
         * Email
         */

//        emailService.sendSimpleEmail(
//
//                leaveRequest
//
//                        .getUser()
//
//                        .getEmail(),
//
//                "Additional Documents Required",
//
//                dto.getComments()
//
//        );
        
        emailService.sendDocumentRequestEmail(

                leaveRequest.getUser().getEmail(),

                leaveRequest.getUser().getFullName(),

                dto.getDocumentType().name(),

                manager.getFullName(),

                dto.getComments()

        );

        /*
         * Audit
         */

        auditService.logAction(

                "DOCUMENT_REQUESTED",

                manager,

                "LEAVE_REQUEST",

                leaveRequest.getId(),

                dto.getDocumentType().name()

        );

        return convertToDto(

                request

        );

    }
    
    private void validateManager(

            LeaveRequest leaveRequest,

            User manager

    ) {

        if (

                leaveRequest

                        .getUser()

                        .getManager()

                        == null

        ) {

            throw new RuntimeException(

                    "Employee does not have manager."

            );

        }

        if (

                !leaveRequest

                        .getUser()

                        .getManager()

                        .getId()

                        .equals(

                                manager.getId()

                        )

        ) {

            throw new RuntimeException(

                    "Only assigned manager can request documents."

            );

        }

    }
    
    private void validateDuplicateRequest(

            Long leaveRequestId,

            CreateDocumentRequestDto dto

    ) {

        boolean exists =

                leaveDocumentRequestRepository
                        .existsByLeaveRequestIdAndDocumentTypeAndStatus(

                                leaveRequestId,

                                dto.getDocumentType(),

                                DocumentRequestStatus.PENDING

                        );

        if (exists) {

            throw new RuntimeException(

                    dto.getDocumentType().name().replace("_", " ")
                            + " has already been requested."

            );

        }

    }
    
    private DocumentRequestDto convertToDto(
            LeaveDocumentRequest request
    ) {

        return DocumentRequestDto.builder()

                .id(request.getId())

                .leaveRequestId(
                        request.getLeaveRequest().getId()
                )

                .requestedBy(
                        request.getRequestedBy().getId()
                )

                .requestedByName(
                        request.getRequestedBy().getFullName()
                )

                .documentType(
                        request.getDocumentType()
                )

                .comments(
                        request.getComments()
                )

                .status(
                        request.getStatus()
                )

                .documentStatus(
                        request.getDocumentStatus()
                )

                .priority(
                        request.getPriority()
                )

                .requestedAt(
                        request.getRequestedAt()
                )

                .completedAt(
                        request.getCompletedAt()
                )
                
                .rejectionComments(

                        request.getRejectionComments()

                )

                .build();

    }
    
    @Transactional(readOnly = true)
    public List<DocumentRequestDto> getDocumentRequests(
            Long leaveRequestId
    ) {

        return leaveDocumentRequestRepository

                .findByLeaveRequestIdOrderByRequestedAtDesc(
                        leaveRequestId
                )

                .stream()

                .map(this::convertToDto)

                .toList();

    }
    
    @Transactional(readOnly = true)
    public List<DocumentRequestDto> getPendingRequests(
            Long leaveRequestId
    ) {

        return leaveDocumentRequestRepository

                .findByLeaveRequestIdAndStatusOrderByRequestedAtDesc(

                        leaveRequestId,

                        DocumentRequestStatus.PENDING

                )

                .stream()

                .map(this::convertToDto)

                .toList();

    }
    
//    public void markCompleted(
//            Long requestId
//    ) {
//
//        LeaveDocumentRequest request =
//
//                leaveDocumentRequestRepository
//
//                        .findById(requestId)
//
//                        .orElseThrow(() ->
//
//                                new ResourceNotFoundException(
//
//                                        "Document request not found"
//
//                                )
//
//                        );
//
//        request.setStatus(
//                DocumentRequestStatus.COMPLETED
//        );
//
//        request.setCompletedAt(
//                LocalDateTime.now()
//        );
//
//        leaveDocumentRequestRepository.save(
//                request
//        );
//
//    }
    
//    public void markCompleted(
//
//            Long leaveRequestId,
//
//            DocumentType documentType
//
//    )
    
    @Transactional
    public LeaveDocumentRequest markCompleted(
            Long leaveRequestId,
            DocumentType documentType
    ) {

        /*
         * 1. Check whether there is already a pending request.
         */
        Optional<LeaveDocumentRequest> pendingRequest =
                leaveDocumentRequestRepository
                        .findFirstByLeaveRequestIdAndDocumentTypeAndStatusOrderByRequestedAtDesc(
                                leaveRequestId,
                                documentType,
                                DocumentRequestStatus.PENDING
                        );

        if (pendingRequest.isPresent()) {

            LeaveDocumentRequest request = pendingRequest.get();

            request.setStatus(DocumentRequestStatus.COMPLETED);
            request.setDocumentStatus(DocumentStatus.UPLOADED);
            request.setCompletedAt(LocalDateTime.now());
            request.setRejectionComments(null);

            return leaveDocumentRequestRepository.save(request);
        }

        /*
         * 2. No pending request.
         *
         * Check the latest request for this document type.
         */
        Optional<LeaveDocumentRequest> latestRequest =
                leaveDocumentRequestRepository
                        .findFirstByLeaveRequestIdAndDocumentTypeOrderByRequestedAtDesc(
                                leaveRequestId,
                                documentType
                        );

        /*
         * 3. If the latest request was rejected,
         *    create a NEW request.
         */
        if (latestRequest.isPresent()
                && latestRequest.get().getDocumentStatus() == DocumentStatus.REJECTED) {

            LeaveDocumentRequest oldRequest = latestRequest.get();

            LeaveDocumentRequest newRequest =
                    LeaveDocumentRequest.builder()
                            .leaveRequest(oldRequest.getLeaveRequest())
                            .requestedBy(oldRequest.getRequestedBy())
                            .documentType(oldRequest.getDocumentType())
                            .comments(oldRequest.getComments())
                            .status(DocumentRequestStatus.COMPLETED)
                            .documentStatus(DocumentStatus.UPLOADED)
                            .priority(oldRequest.getPriority())
                            .requestedAt(LocalDateTime.now())
                            .completedAt(LocalDateTime.now())
                            .rejectionComments(null)
                            .build();

            return leaveDocumentRequestRepository.save(newRequest);
        }

        /*
         * 4. No request exists.
         *
         * This can happen when the employee uploads
         * a document voluntarily.
         *
         * Return null so the upload itself still succeeds.
         */
        return null;
    }
    
    @Transactional(readOnly = true)
    public List<DocumentRequestDto> getPendingRequestsByEmployee(Long userId) {

        return leaveDocumentRequestRepository
                .findByLeaveRequestUserIdAndStatusOrderByRequestedAtDesc(
                        userId,
                        DocumentRequestStatus.PENDING
                )
                .stream()
                .map(this::convertToDto)
                .toList();

    }
    
    @Transactional
    public void verifyDocument(Long requestId) {

        LeaveDocumentRequest request =
                leaveDocumentRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document request not found"
                                ));

        request.setDocumentStatus(
                DocumentStatus.VERIFIED
        );
        
        leaveDocumentRequestRepository.save(request);
        
        auditService.logAction(

                "DOCUMENT_VERIFIED",

                request.getRequestedBy(),

                "LEAVE_REQUEST",

                request.getLeaveRequest().getId(),

                request.getDocumentType().name()

        );
        
        notificationService.createNotification(

                request.getLeaveRequest()
                        .getUser()
                        .getId(),

                "Document Verified",

                "Your uploaded "
                        + request.getDocumentType().name().replace("_"," ")
                        + " has been verified."

        );
        
        emailService.sendDocumentVerifiedEmail(

                request.getLeaveRequest()
                        .getUser()
                        .getEmail(),

                request.getLeaveRequest()
                        .getUser()
                        .getFullName(),

                request.getDocumentType().name(),

                request.getRequestedBy()
                        .getFullName()

        );

      

    }
    
    @Transactional
    public void rejectDocument(

            Long requestId,

            RejectDocumentDto dto

    ) {

        LeaveDocumentRequest request =
                leaveDocumentRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document request not found"
                                ));

        request.setDocumentStatus(DocumentStatus.REJECTED);
        request.setStatus(DocumentRequestStatus.COMPLETED);
        
        request.setRejectionComments(

                dto.getRejectionComments()

        );
        
        leaveDocumentRequestRepository.save(request);
        
        auditService.logAction(

                "DOCUMENT_REJECTED",

                request.getRequestedBy(),

                "LEAVE_REQUEST",

                request.getLeaveRequest().getId(),

                dto.getRejectionComments()

        );
        
        notificationService.createNotification(

                request.getLeaveRequest()
                        .getUser()
                        .getId(),

                "Document Rejected",

                "Please upload a valid "
                        + request.getDocumentType().name().replace("_"," ")
                        + "."

        );
        
        emailService.sendDocumentRejectedEmail(

                request.getLeaveRequest()
                        .getUser()
                        .getEmail(),

                request.getLeaveRequest()
                        .getUser()
                        .getFullName(),

                request.getDocumentType().name(),

                request.getRequestedBy()
                        .getFullName(),

                        request.getRejectionComments()

        );

 

    }
    
    @Transactional(readOnly = true)
    public List<EmployeeActionDto> getEmployeeActions(

            Long employeeId

    ) {

        return leaveDocumentRequestRepository

                .findByLeaveRequestUserIdAndStatusOrderByRequestedAtDesc(

                        employeeId,

                        DocumentRequestStatus.PENDING

                )

                .stream()

                .map(request ->

                        EmployeeActionDto.builder()

                                .requestId(
                                        request.getId()
                                )

                                .leaveRequestId(
                                        request.getLeaveRequest().getId()
                                )

                                .documentType(
                                        request.getDocumentType()
                                )

                                .priority(
                                        request.getPriority()
                                )

                                .requestedBy(
                                        request.getRequestedBy().getFullName()
                                )

                                .requestedAt(
                                        request.getRequestedAt()
                                )

                                .comments(
                                        request.getComments()
                                )

                                .build()

                )

                .toList();

    }
    
    @Transactional(readOnly = true)
    public List<EmployeeActionDto> getManagerPendingActions(Long managerId) {

        return leaveDocumentRequestRepository
                .findByLeaveRequestUserIdAndStatusOrderByRequestedAtDesc(
                        managerId,
                        DocumentRequestStatus.PENDING
                )
                .stream()
                .map(request ->
                        EmployeeActionDto.builder()
                                .requestId(request.getId())
                                .leaveRequestId(
                                        request.getLeaveRequest().getId()
                                )
                                .documentType(
                                        request.getDocumentType()
                                )
                                .priority(
                                        request.getPriority()
                                )
                                .requestedBy(
                                        request.getRequestedBy().getFullName()
                                )
                                .requestedAt(
                                        request.getRequestedAt()
                                )
                                .comments(
                                        request.getComments()
                                )
                                .build()
                )
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ManagerActionDto> getManagerActions(
            Long managerId
    ) {

        return leaveDocumentRequestRepository

                .findByRequestedByIdAndDocumentStatusOrderByCompletedAtDesc(

                        managerId,

                        DocumentStatus.UPLOADED

                )

                .stream()

                .map(request ->

                        ManagerActionDto.builder()

                                .requestId(
                                        request.getId()
                                )

                                .leaveRequestId(
                                        request.getLeaveRequest().getId()
                                )

                                .employeeName(
                                        request.getLeaveRequest()
                                               .getUser()
                                               .getFullName()
                                )

                                .documentType(
                                        request.getDocumentType()
                                )

                                .priority(
                                        request.getPriority()
                                )

                                .uploadedAt(
                                        request.getCompletedAt()
                                )

                                .uploadedBy(
                                        request.getLeaveRequest()
                                               .getUser()
                                               .getFullName()
                                )

                                .build()

                )

                .toList();

    }
    
    @Transactional(readOnly = true)
    public List<ManagerActionDto> getAdminActions(Long adminId) {

        return leaveDocumentRequestRepository
                .findByRequestedByIdAndDocumentStatusOrderByCompletedAtDesc(
                        adminId,
                        DocumentStatus.UPLOADED
                )
                .stream()
                .map(request ->
                        ManagerActionDto.builder()
                                .requestId(request.getId())
                                .leaveRequestId(
                                        request.getLeaveRequest().getId()
                                )
                                .employeeName(
                                        request.getLeaveRequest()
                                                .getUser()
                                                .getFullName()
                                )
                                .documentType(
                                        request.getDocumentType()
                                )
                                .priority(
                                        request.getPriority()
                                )
                                .uploadedAt(
                                        request.getCompletedAt()
                                )
                                .uploadedBy(
                                        request.getLeaveRequest()
                                                .getUser()
                                                .getFullName()
                                )
                                .build()
                )
                .toList();
    }
    
}
    
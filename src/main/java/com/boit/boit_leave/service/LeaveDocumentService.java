package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.LeaveDocumentDto;
import com.boit.boit_leave.entity.DocumentStatus;
import com.boit.boit_leave.entity.DocumentType;
import com.boit.boit_leave.entity.LeaveDocument;
import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.exception.AccessDeniedException;
import com.boit.boit_leave.exception.ResourceNotFoundException;
import com.boit.boit_leave.repository.LeaveDocumentRepository;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import com.boit.boit_leave.entity.LeaveDocumentRequest;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.HashMap;
import java.util.Map;
import com.boit.boit_leave.AI.service.DocumentAnalysisBackgroundService;
import com.boit.boit_leave.AI.dto.DocumentAnalysisResponse;
import com.boit.boit_leave.AI.dto.DocumentAnalysisStatusResponse;
import com.boit.boit_leave.entity.AiAnalysisStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveDocumentService {

    @Value("${leave.document.upload.path}")
    private String uploadPath;

    private final LeaveDocumentRepository leaveDocumentRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final UserRepository userRepository;

    private final FileValidationUtil fileValidationUtil;
    
    private final LeaveDocumentRequestService
    leaveDocumentRequestService;
    
    private final AuditService auditService;
    
    private final EmailService emailService;
    
    private final DocumentAnalysisBackgroundService
    documentAnalysisBackgroundService;
    
    private final ObjectMapper objectMapper;

    public LeaveDocumentDto uploadDocument(

            Long leaveRequestId,

            Long uploadedBy,

            MultipartFile file,

            DocumentType documentType,

            String remarks

    ) throws IOException {

        /*
         * Validate File
         */
        fileValidationUtil.validate(file);

        /*
         * Fetch Leave Request
         */
//        LeaveRequest leaveRequest =
//                leaveRequestRepository
//                        .findById(leaveRequestId)
//                        .orElseThrow(() ->
//                                new RuntimeException("Leave request not found"));
        
        LeaveRequest leaveRequest =
                validateLeaveAccess(
                        leaveRequestId,
                        uploadedBy
                );

        /*
         * Fetch User
         */
        User user =
                userRepository
                        .findById(uploadedBy)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

        /*
         * Create Upload Folder
         */
        Path uploadDirectory =
                Paths.get(uploadPath);

        if (!Files.exists(uploadDirectory)) {

            Files.createDirectories(uploadDirectory);

        }

        /*
         * Generate Unique File Name
         */
        String extension =
                StringUtils.getFilenameExtension(
                        file.getOriginalFilename()
                );

        String storedFileName =
                UUID.randomUUID()
                        + "."
                        + extension;

        Path destination =
                uploadDirectory.resolve(
                        storedFileName
                );

        /*
         * Save Physical File
         */
        Files.copy(

                file.getInputStream(),

                destination

        );

        /*
         * Save Database Record
         */
        LeaveDocument leaveDocument =
                LeaveDocument.builder()

                        .leaveRequest(leaveRequest)
                        
                        .documentType(documentType)

                        .status(DocumentStatus.UPLOADED)

                        .originalFileName(
                                file.getOriginalFilename()
                        )

                        .storedFileName(
                                storedFileName
                        )

                        .filePath(
                                destination.toString()
                        )

                        .contentType(
                                file.getContentType()
                        )

                        .fileSize(
                                file.getSize()
                        )

                        .remarks(
                                remarks
                        )

                        .uploadedBy(
                                user
                        )

                        .uploadedAt(
                                LocalDateTime.now()
                        )

                        .build();

        leaveDocument =
                leaveDocumentRepository.save(
                        leaveDocument
                );
        
     // Start AI analysis only after the upload transaction commits
        Long documentId = leaveDocument.getId();

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    documentAnalysisBackgroundService
                            .analyzeInBackground(documentId);
                }
            }
        );
        
        auditService.logAction(

                "DOCUMENT_UPLOADED",

                user,

                "LEAVE_REQUEST",

                leaveRequest.getId(),

                leaveDocument.getDocumentType().name()

        );
        
        /*
         * Automatically complete pending document request
         * and notify the manager who requested the document.
         */
        try {
            LeaveDocumentRequest completedRequest =
                    leaveDocumentRequestService
                            .markCompleted(
                                    leaveRequest.getId(),
                                    documentType
                            );

            /*
             * Link this physical document
             * to the exact document request.
             */
            if (completedRequest != null) {

                leaveDocument.setDocumentRequest(completedRequest);

                leaveDocument =
                        leaveDocumentRepository.save(leaveDocument);
            }

            if (completedRequest != null
                    && completedRequest.getRequestedBy() != null
                    && completedRequest.getRequestedBy().getEmail() != null) {

                User manager =
                        completedRequest.getRequestedBy();

                Map<String, String> emailVariables =
                        new HashMap<>();

                emailVariables.put(
                        "managerName",
                        manager.getFullName()
                );

                emailVariables.put(
                        "employeeName",
                        leaveRequest.getUser().getFullName()
                );

                emailVariables.put(
                        "requestId",
                        leaveRequest.getId().toString()
                );

                emailVariables.put(
                        "leaveType",
                        leaveRequest.getLeaveType().name()
                );

                emailVariables.put(
                        "startDate",
                        leaveRequest.getStartDate().toString()
                );

                emailVariables.put(
                        "endDate",
                        leaveRequest.getEndDate().toString()
                );

                emailVariables.put(
                        "documentType",
                        documentType.name()
                );

                emailVariables.put(
                        "uploadedAt",
                        leaveDocument.getUploadedAt().toString()
                );

                emailService.sendTemplateEmail(
                        manager.getEmail(),
                        "LEAVE_DOCUMENT_UPLOADED",
                        emailVariables
                );
            }

        } catch (Exception ex) {
            /*
             * Document may have been uploaded voluntarily
             * without a manager request.
             *
             * Do not fail the document upload because
             * notification/request processing failed.
             */
        }
        

        return LeaveDocumentDto.builder()

                .id(
                        leaveDocument.getId()
                )

                .leaveRequestId(
                        leaveRequest.getId()
                )
                
                .documentRequestId(
                        leaveDocument.getDocumentRequest() != null
                                ? leaveDocument.getDocumentRequest().getId()
                                : null
                )

                .documentType(
                        leaveDocument.getDocumentType()
                )

                .status(
                        leaveDocument.getStatus()
                )

                .originalFileName(
                        leaveDocument.getOriginalFileName()
                )

                .contentType(
                        leaveDocument.getContentType()
                )

                .fileSize(
                        leaveDocument.getFileSize()
                )

                .remarks(
                        leaveDocument.getRemarks()
                )

                .uploadedBy(
                        user.getId()
                )

                .uploadedByName(
                        user.getFullName()
                )

                .uploadedAt(
                        leaveDocument.getUploadedAt()
                )

                .build();

    }
    
    @Transactional(readOnly = true)
    public List<LeaveDocumentDto>
    getDocumentsByLeaveRequest(

            Long leaveRequestId,

            Long loggedInUserId

    ) {

        validateLeaveAccess(

                leaveRequestId,

                loggedInUserId

        );

        return leaveDocumentRepository

                .findByLeaveRequestIdOrderByUploadedAtDesc(
                        leaveRequestId
                )

                .stream()

                .map(document ->

                        LeaveDocumentDto.builder()

                                .id(document.getId())

                                .leaveRequestId(
                                        document.getLeaveRequest().getId())
                                
                                .documentRequestId(
                                        document.getDocumentRequest() != null
                                                ? document.getDocumentRequest().getId()
                                                : null
                                )

                                .documentType(
                                        document.getDocumentType())

                                .status(
                                        document.getStatus())

                                .originalFileName(
                                        document.getOriginalFileName())

                                .contentType(
                                        document.getContentType())

                                .fileSize(
                                        document.getFileSize())

                                .remarks(
                                        document.getRemarks())

                                .uploadedBy(
                                        document.getUploadedBy().getId())

                                .uploadedByName(
                                        document.getUploadedBy().getFullName())

                                .uploadedAt(
                                        document.getUploadedAt())

                                .build()

                )

                .toList();

    }
    
    @Transactional(readOnly = true)
    public Resource downloadDocument(

            Long documentId,

            Long loggedInUserId

    ) throws IOException {

        LeaveDocument document =
                leaveDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"));

        validateLeaveAccess(
                document.getLeaveRequest().getId(),
                loggedInUserId
        );

        Path path =
                Paths.get(document.getFilePath());

        Resource resource =
                new UrlResource(path.toUri());

        if (!resource.exists()) {

            throw new ResourceNotFoundException(
                    "Document file not found."
            );

        }

        return resource;

    }
    
    private LeaveRequest validateLeaveAccess(

            Long leaveRequestId,

            Long loggedInUserId

    ) {

        LeaveRequest leaveRequest =
                leaveRequestRepository
                        .findById(leaveRequestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave request not found"));

        User loggedInUser =
                userRepository
                        .findById(loggedInUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        /*
         * ADMIN
         */
        if (loggedInUser.getRole().name().equals("ADMIN")) {

            return leaveRequest;

        }

        /*
         * EMPLOYEE
         */
        if (loggedInUser.getRole().name().equals("EMPLOYEE")) {

            if (!leaveRequest.getUser()
                    .getId()
                    .equals(loggedInUserId)) {

            	throw new AccessDeniedException(
            	        "You are not authorized to access this leave request.");
            }

            return leaveRequest;

        }

        /*
         * MANAGER
         */
        if (loggedInUser.getRole().name().equals("MANAGER")) {

            User employee = leaveRequest.getUser();

            // Manager's own leave request
            if (employee.getId().equals(loggedInUserId)) {
                return leaveRequest;
            }

            // Employee under this manager
            if (employee.getManager() == null ||
                !employee.getManager().getId().equals(loggedInUserId)) {

                throw new AccessDeniedException(
                        "You are not authorized to access this leave request."
                );
            }

            return leaveRequest;
        }

    	throw new AccessDeniedException(
    	        "You are not authorized to access this leave request.");

    }
    
    @Transactional(readOnly = true)
    public LeaveDocument getDocumentForAnalysis(
            Long documentId,
            Long loggedInUserId
    ) throws IOException {

        LeaveDocument document =
                leaveDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"
                                )
                        );

        // Validate that the logged-in manager is allowed
        // to access this leave request
        validateLeaveAccess(
                document.getLeaveRequest().getId(),
                loggedInUserId
        );

        Path path =
                Paths.get(document.getFilePath());

        if (!Files.exists(path)) {
            throw new ResourceNotFoundException(
                    "Document file not found."
            );
        }

        return document;
    }
    

@Transactional(readOnly = true)
public DocumentAnalysisStatusResponse getAiAnalysis(
        Long documentId,
        Long loggedInUserId
) {

    LeaveDocument document = leaveDocumentRepository
            .findById(documentId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Document not found"
                    )
            );

    // Reuse the existing employee/manager/admin access validation.
    validateLeaveAccess(
            document.getLeaveRequest().getId(),
            loggedInUserId
    );

    DocumentAnalysisResponse analysis = null;

    if (document.getAiAnalysisResult() != null
            && !document.getAiAnalysisResult().isBlank()) {
        try {
            analysis = objectMapper.readValue(
                    document.getAiAnalysisResult(),
                    DocumentAnalysisResponse.class
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to read saved AI analysis.",
                    ex
            );
        }
    }

    return DocumentAnalysisStatusResponse.builder()
            .documentId(document.getId())
            .status(document.getAiAnalysisStatus())
            .analysis(analysis)
            .errorMessage(document.getAiAnalysisError())
            .completedAt(document.getAiAnalysisCompletedAt())
            .build();
}


}
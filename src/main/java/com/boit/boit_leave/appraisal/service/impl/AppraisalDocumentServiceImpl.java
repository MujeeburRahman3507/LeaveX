package com.boit.boit_leave.appraisal.service.impl;

import com.boit.boit_leave.appraisal.dto.AppraisalDocumentResponse;
import com.boit.boit_leave.appraisal.entity.AppraisalDocument;
import com.boit.boit_leave.appraisal.entity.AppraisalGoal;
import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.repository.AppraisalDocumentRepository;
import com.boit.boit_leave.appraisal.repository.AppraisalGoalRepository;
import com.boit.boit_leave.appraisal.repository.EmployeeAppraisalRepository;
import com.boit.boit_leave.appraisal.service.AppraisalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppraisalDocumentServiceImpl
        implements AppraisalDocumentService {

    private final AppraisalDocumentRepository appraisalDocumentRepository;
    private final EmployeeAppraisalRepository employeeAppraisalRepository;
    private final AppraisalGoalRepository appraisalGoalRepository;

    @Value("${appraisal.document.upload-dir:uploads/appraisal}")
    private String uploadDirectory;

    @Override
    public AppraisalDocumentResponse uploadDocument(
            Long appraisalId,
            Long goalId,
            MultipartFile file,
            String documentType,
            String remarks,
            Long uploadedBy
    ) {

        if (appraisalId == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        if (uploadedBy == null) {
            throw new IllegalArgumentException(
                    "Uploaded-by user ID is required"
            );
        }

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Document file is required"
            );
        }

        EmployeeAppraisal appraisal =
                employeeAppraisalRepository
                        .findById(appraisalId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Appraisal not found: "
                                                + appraisalId
                                )
                        );

        AppraisalGoal goal = null;

        if (goalId != null) {

            goal = appraisalGoalRepository
                    .findByIdAndAppraisalId(
                            goalId,
                            appraisalId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Goal does not belong to this appraisal"
                            )
                    );
        }

        validateFile(file);

        try {

            Path uploadPath =
                    Paths.get(uploadDirectory)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadPath);

            String originalFileName =
                    file.getOriginalFilename() != null
                            ? file.getOriginalFilename()
                            : "document";

            String storedFileName =
                    UUID.randomUUID()
                            + "_"
                            + sanitizeFileName(
                                    originalFileName
                            );

            Path targetPath =
                    uploadPath.resolve(
                            storedFileName
                    ).normalize();

            /*
             * Prevent path traversal.
             */
            if (!targetPath.startsWith(uploadPath)) {
                throw new IllegalArgumentException(
                        "Invalid file path"
                );
            }

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            AppraisalDocument document =
                    AppraisalDocument.builder()
                            .appraisal(appraisal)
                            .goal(goal)
                            .uploadedBy(uploadedBy)
                            .originalFileName(
                                    originalFileName
                            )
                            .storedFileName(
                                    storedFileName
                            )
                            .filePath(
                                    targetPath.toString()
                            )
                            .contentType(
                                    file.getContentType()
                            )
                            .fileSize(
                                    file.getSize()
                            )
                            .documentType(
                                    documentType
                            )
                            .remarks(
                                    remarks
                            )
                            .build();

            return toResponse(
                    appraisalDocumentRepository.save(
                            document
                    )
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Failed to store appraisal document",
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppraisalDocumentResponse> getAppraisalDocuments(
            Long appraisalId
    ) {

        if (appraisalId == null) {
            throw new IllegalArgumentException(
                    "Appraisal ID is required"
            );
        }

        if (!employeeAppraisalRepository
                .existsById(appraisalId)) {

            throw new IllegalArgumentException(
                    "Appraisal not found: "
                            + appraisalId
            );
        }

        return appraisalDocumentRepository
                .findByAppraisalIdOrderByCreatedAtDesc(
                        appraisalId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppraisalDocumentResponse> getGoalDocuments(
            Long goalId
    ) {

        if (goalId == null) {
            throw new IllegalArgumentException(
                    "Goal ID is required"
            );
        }

        if (!appraisalGoalRepository
                .existsById(goalId)) {

            throw new IllegalArgumentException(
                    "Goal not found: " + goalId
            );
        }

        return appraisalDocumentRepository
                .findByGoalIdOrderByCreatedAtDesc(
                        goalId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteDocument(
            Long documentId,
            Long userId
    ) {

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "Document ID is required"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        AppraisalDocument document =
                appraisalDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Document not found: "
                                                + documentId
                                )
                        );

        if (!userId.equals(
                document.getUploadedBy()
        )) {

            throw new IllegalStateException(
                    "You are not authorized to delete this document"
            );
        }

        try {

            if (document.getFilePath() != null) {

                Path filePath =
                        Paths.get(
                                document.getFilePath()
                        );

                Files.deleteIfExists(filePath);
            }

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Failed to delete document file",
                    ex
            );
        }

        appraisalDocumentRepository.delete(
                document
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Path getDocumentForView(
            Long documentId,
            Long userId,
            String role
    ) {

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "Document ID is required"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        AppraisalDocument document =
                appraisalDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Document not found: "
                                                + documentId
                                )
                        );

        EmployeeAppraisal appraisal =
                document.getAppraisal();

        if (appraisal == null) {
            throw new IllegalStateException(
                    "Document is not linked to an appraisal"
            );
        }

        /*
         * ADMIN / HR
         * Can view all appraisal documents.
         */
        if ("ADMIN".equalsIgnoreCase(role)) {
            // allowed
        }

        /*
         * MANAGER
         * Can view documents belonging to
         * employees assigned to this manager.
         */
        else if ("MANAGER".equalsIgnoreCase(role)) {

            if (appraisal.getManagerId() == null ||
                    !userId.equals(appraisal.getManagerId())) {

                throw new IllegalStateException(
                        "You are not authorized to view this document"
                );
            }
        }

        /*
         * EMPLOYEE
         * Can view documents belonging to
         * their own appraisal.
         */
        else if ("EMPLOYEE".equalsIgnoreCase(role)) {

            if (appraisal.getEmployeeId() == null ||
                    !userId.equals(appraisal.getEmployeeId())) {

                throw new IllegalStateException(
                        "You are not authorized to view this document"
                );
            }
        }

        else {
            throw new IllegalStateException(
                    "You are not authorized to view this document"
            );
        }

        if (document.getFilePath() == null ||
                document.getFilePath().isBlank()) {

            throw new IllegalStateException(
                    "Document file path is not available"
            );
        }

        try {

            Path uploadPath =
                    Paths.get(uploadDirectory)
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    Paths.get(document.getFilePath())
                            .toAbsolutePath()
                            .normalize();

            /*
             * Prevent path traversal / access outside
             * the appraisal upload directory.
             */
            if (!filePath.startsWith(uploadPath)) {

                throw new IllegalStateException(
                        "Invalid document path"
                );
            }

            if (!Files.exists(filePath) ||
                    !Files.isRegularFile(filePath)) {

                throw new IllegalArgumentException(
                        "Document file does not exist"
                );
            }

            return filePath;

        } catch (Exception ex) {

            if (ex instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) ex;
            }

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            throw new IllegalStateException(
                    "Unable to access document",
                    ex
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {

        /*
         * Maximum file size:
         * 10 MB
         */
        long maxSize =
                10 * 1024 * 1024;

        if (file.getSize() > maxSize) {

            throw new IllegalArgumentException(
                    "File size cannot exceed 10 MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException(
                    "Unable to determine file type"
            );
        }

        if (!contentType.equals("application/pdf")
                && !contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("application/msword")
                && !contentType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                && !contentType.equals("application/vnd.ms-excel")
                && !contentType.equals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                && !contentType.equals("application/vnd.ms-powerpoint")
                && !contentType.equals(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation")
                && !contentType.equals("application/vnd.ms-outlook")
                && !contentType.equals("message/rfc822")
        ) {
            throw new IllegalArgumentException(
                    "Unsupported document type"
            );
        }
    }

    private String sanitizeFileName(
            String fileName
    ) {

        return fileName
                .replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );
    }

    private AppraisalDocumentResponse toResponse(
            AppraisalDocument document
    ) {

        return AppraisalDocumentResponse.builder()
                .id(document.getId())
                .appraisalId(
                        document.getAppraisal() != null
                                ? document.getAppraisal().getId()
                                : null
                )
                .goalId(
                        document.getGoal() != null
                                ? document.getGoal().getId()
                                : null
                )
                .uploadedBy(
                        document.getUploadedBy()
                )
                .originalFileName(
                        document.getOriginalFileName()
                )
                .storedFileName(
                        document.getStoredFileName()
                )
                .contentType(
                        document.getContentType()
                )
                .fileSize(
                        document.getFileSize()
                )
                .documentType(
                        document.getDocumentType()
                )
                .remarks(
                        document.getRemarks()
                )
                .createdAt(
                        document.getCreatedAt()
                )
                .build();
    }
}
package com.boit.boit_leave.appraisal.controller;

import com.boit.boit_leave.appraisal.dto.AppraisalDocumentResponse;
import com.boit.boit_leave.appraisal.service.AppraisalDocumentService;
import com.boit.boit_leave.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;
import java.nio.file.Files;

import java.util.List;

@RestController
@RequestMapping("/api/appraisal/documents")
@RequiredArgsConstructor
public class AppraisalDocumentController {

    private final AppraisalDocumentService appraisalDocumentService;

    /**
     * Upload an appraisal document.
     *
     * Employee can upload supporting documents
     * for their appraisal.
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AppraisalDocumentResponse> uploadDocument(
            @RequestParam Long appraisalId,
            @RequestParam(required = false) Long goalId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String remarks,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        AppraisalDocumentResponse response =
                appraisalDocumentService.uploadDocument(
                        appraisalId,
                        goalId,
                        file,
                        documentType,
                        remarks,
                        userDetails.getUserId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get all documents belonging to an appraisal.
     */
    @GetMapping("/appraisal/{appraisalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppraisalDocumentResponse>>
    getAppraisalDocuments(
            @PathVariable Long appraisalId
    ) {

        return ResponseEntity.ok(
                appraisalDocumentService.getAppraisalDocuments(
                        appraisalId
                )
        );
    }

    /**
     * Get all documents belonging to a specific goal.
     */
    @GetMapping("/goal/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppraisalDocumentResponse>>
    getGoalDocuments(
            @PathVariable Long goalId
    ) {

        return ResponseEntity.ok(
                appraisalDocumentService.getGoalDocuments(
                        goalId
                )
        );
    }

    /**
     * Delete an uploaded document.
     *
     * Only the user who uploaded the document
     * can delete it.
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        appraisalDocumentService.deleteDocument(
                documentId,
                userDetails.getUserId()
        );

        return ResponseEntity.noContent().build();
    }

    private CustomUserDetails getCustomUserDetails(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {

            throw new IllegalStateException(
                    "Authenticated user details are not available"
            );
        }

        return (CustomUserDetails) principal;
    }
    
    @GetMapping("/{documentId}/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                getCustomUserDetails(authentication);

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority()
                        )
                        .orElse("");

        role = role.replace("ROLE_", "");

        Path filePath =
                appraisalDocumentService.getDocumentForView(
                        documentId,
                        userDetails.getUserId(),
                        role
                );

        try {

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists() ||
                    !resource.isReadable()) {

                throw new IllegalStateException(
                        "Document cannot be read"
                );
            }

            String contentType =
                    Files.probeContentType(filePath);

            if (contentType == null) {
                contentType =
                        "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    filePath.getFileName() +
                                    "\""
                    )
                    .header(
                            HttpHeaders.CONTENT_TYPE,
                            contentType
                    )
                    .body(resource);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to view document",
                    ex
            );
        }
    }
}
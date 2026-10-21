package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.ApiResponse;
import com.boit.boit_leave.dto.LeaveDocumentDto;
import com.boit.boit_leave.entity.DocumentType;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.LeaveDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import com.boit.boit_leave.AI.dto.DocumentAnalysisStatusResponse;

@RestController
@RequestMapping("/api/leave/document")
//@RequestMapping("/api/employee/leave/document")
@RequiredArgsConstructor
public class LeaveDocumentController {

    private final LeaveDocumentService leaveDocumentService;

//    @PostMapping("/upload")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<LeaveDocumentDto>>
    uploadDocument(

            @RequestParam Long leaveRequestId,

            @RequestParam DocumentType documentType,

            @RequestParam(required = false)
            String remarks,

            @RequestParam MultipartFile file,

            @AuthenticationPrincipal
            CustomUserDetails user

    ) throws Exception {

        LeaveDocumentDto response =

                leaveDocumentService.uploadDocument(

                        leaveRequestId,

                        user.getUserId(),

                        file,

                        documentType,

                        remarks

                );

        return ResponseEntity.ok(

                ApiResponse.<LeaveDocumentDto>builder()

                        .success(true)

                        .message("Document Uploaded Successfully")

                        .data(response)

                        .build()

        );

    }
    
    @GetMapping("/leave/{leaveRequestId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<LeaveDocumentDto>>>
    getDocuments(

            @PathVariable
            Long leaveRequestId,

            @AuthenticationPrincipal
            CustomUserDetails user

    ) {

        return ResponseEntity.ok(

                ApiResponse
                        .<List<LeaveDocumentDto>>builder()

                        .success(true)

                        .message("Documents Loaded")

                        .data(

                                leaveDocumentService
                                        .getDocumentsByLeaveRequest(

                                                leaveRequestId,

                                                user.getUserId()

                                        )

                        )

                        .build()

        );

    }
    
    @GetMapping("/view/{documentId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<Resource>
    viewDocument(

            @PathVariable
            Long documentId,

            @AuthenticationPrincipal
            CustomUserDetails user

    ) throws Exception {

        Resource resource =

                leaveDocumentService
                        .downloadDocument(

                                documentId,

                                user.getUserId()

                        );

        String contentType =
                Files.probeContentType(
                        resource.getFile().toPath()
                );

        if (contentType == null) {

            String filename =
                    resource.getFilename()
                            .toLowerCase();

            if (filename.endsWith(".pdf")) {

                contentType = "application/pdf";

            } else if (filename.endsWith(".png")) {

                contentType = "image/png";

            } else if (
                    filename.endsWith(".jpg")
                    || filename.endsWith(".jpeg")) {

                contentType = "image/jpeg";

            } else {

                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;

            }

        }

        return ResponseEntity.ok()

                .contentType(
                        MediaType.parseMediaType(
                                contentType
                        )
                )

                .header(

                        HttpHeaders.CONTENT_DISPOSITION,

                        "inline; filename=\""

                                + resource.getFilename()

                                + "\""

                )

                .body(resource);

    }
    
    @GetMapping("/download/{documentId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<Resource>
    downloadDocument(

            @PathVariable
            Long documentId,

            @AuthenticationPrincipal
            CustomUserDetails user

    ) throws Exception {

        Resource resource =
                leaveDocumentService
                        .downloadDocument(
                                documentId,
                                user.getUserId()
                        );

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + resource.getFilename()
                                + "\""
                )

                .body(resource);

    }
    

@GetMapping("/ai-analysis/{documentId}")
@PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
public ResponseEntity<ApiResponse<DocumentAnalysisStatusResponse>>
getAiAnalysis(
        @PathVariable Long documentId,
        @AuthenticationPrincipal CustomUserDetails user
) {

    DocumentAnalysisStatusResponse response =
            leaveDocumentService.getAiAnalysis(
                    documentId,
                    user.getUserId()
            );

    return ResponseEntity.ok(
            ApiResponse.<DocumentAnalysisStatusResponse>builder()
                    .success(true)
                    .message("AI analysis status loaded")
                    .data(response)
                    .build()
    );
}


}
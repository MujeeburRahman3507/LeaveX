package com.boit.boit_leave.AI.controller;

import com.boit.boit_leave.AI.dto.DocumentAnalysisResponse;
import com.boit.boit_leave.AI.dto.ManagerAiRequest;
import com.boit.boit_leave.AI.dto.ManagerAiResponse;
import com.boit.boit_leave.AI.service.DocumentIntelligenceService;
import com.boit.boit_leave.AI.dto.AdminAiRequest;
import com.boit.boit_leave.AI.dto.AdminAiResponse;
import com.boit.boit_leave.AI.service.ManagerAiService;
import com.boit.boit_leave.AI.service.AdminAiService;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.AI.dto.EmployeeAiRequest;
import com.boit.boit_leave.AI.dto.EmployeeAiResponse;
import com.boit.boit_leave.AI.service.EmployeeAiService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import com.boit.boit_leave.entity.LeaveDocument;
import com.boit.boit_leave.service.LeaveDocumentService;


@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {


    private final DocumentIntelligenceService documentIntelligenceService;

    private final ManagerAiService managerAiService;
    
    private final AdminAiService adminAiService;

    private final EmployeeAiService employeeAiService;

    private final LeaveDocumentService leaveDocumentService;
    
    @PostMapping(
        value = "/analyze-document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentAnalysisResponse> analyze(

            @RequestParam MultipartFile file) {

        return ResponseEntity.ok(
        		documentIntelligenceService.analyze(file)
        );
    }

    @PostMapping("/analyze-document/{documentId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DocumentAnalysisResponse> analyzeExistingDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) throws Exception {

        LeaveDocument document =
                leaveDocumentService.getDocumentForAnalysis(
                        documentId,
                        user.getUserId()
                );

        return ResponseEntity.ok(
                documentIntelligenceService.analyzeDocument(
                        document
                )
        );
    }

    @PostMapping("/manager-assistant")
    public ResponseEntity<ManagerAiResponse>
    managerAssistant(

            @AuthenticationPrincipal
            CustomUserDetails user,

            @RequestBody ManagerAiRequest request) {


        ManagerAiResponse response =
                managerAiService.processQuestion(
                        user.getUserId(),
                        request.getPrompt()
                );


        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin-assistant")
    public ResponseEntity<AdminAiResponse>
    adminAssistant(
            @AuthenticationPrincipal
            CustomUserDetails user,
            @RequestBody AdminAiRequest request) {

        AdminAiResponse response =
                adminAiService.processQuestion(
                        request.getPrompt()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/employee-assistant")
    public ResponseEntity<EmployeeAiResponse> employeeAssistant(

            @AuthenticationPrincipal
            CustomUserDetails user,

            @RequestBody
            EmployeeAiRequest request) {

        EmployeeAiResponse response =
                employeeAiService.processQuestion(
                        user.getUserId(),
                        request.getPrompt()
                );

        return ResponseEntity.ok(response);
    }
}
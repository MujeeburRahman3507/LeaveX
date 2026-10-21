package com.boit.boit_leave.appraisal.service;

import com.boit.boit_leave.appraisal.dto.AppraisalDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.nio.file.Path;

public interface AppraisalDocumentService {

    AppraisalDocumentResponse uploadDocument(
            Long appraisalId,
            Long goalId,
            MultipartFile file,
            String documentType,
            String remarks,
            Long uploadedBy
    );

    List<AppraisalDocumentResponse> getAppraisalDocuments(
            Long appraisalId
    );

    List<AppraisalDocumentResponse> getGoalDocuments(
            Long goalId
    );

    void deleteDocument(
            Long documentId,
            Long userId
    );
    
    Path getDocumentForView(
            Long documentId,
            Long userId,
            String role
    );
}
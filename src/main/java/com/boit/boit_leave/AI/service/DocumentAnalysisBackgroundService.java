
package com.boit.boit_leave.AI.service;

import com.boit.boit_leave.AI.dto.DocumentAnalysisResponse;
import com.boit.boit_leave.entity.AiAnalysisStatus;
import com.boit.boit_leave.entity.LeaveDocument;
import com.boit.boit_leave.repository.LeaveDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentAnalysisBackgroundService {

    private final LeaveDocumentRepository leaveDocumentRepository;
    private final DocumentIntelligenceService documentIntelligenceService;
    private final ObjectMapper objectMapper;


    
    @Async("documentAnalysisExecutor")
    public void analyzeInBackground(Long documentId) {
        log.info("Starting background AI analysis for document ID: {}",
                documentId);
        try {
            LeaveDocument document = leaveDocumentRepository
                    .findById(documentId)
                    .orElse(null);

            if (document == null) {
                log.error("Document ID {} not found for AI analysis", documentId);
                return;
            }

            // Mark analysis as processing
            document.setAiAnalysisStatus(AiAnalysisStatus.PROCESSING);
            document.setAiAnalysisError(null);

            leaveDocumentRepository.saveAndFlush(document);
            
            log.info("Calling AI analysis for document ID: {}", documentId);

            // Run existing AI analysis
            DocumentAnalysisResponse result =
                    documentIntelligenceService.analyzeDocument(document);

            // Reload the document before saving the result
            document = leaveDocumentRepository
                    .findById(documentId)
                    .orElseThrow();

            // Store the complete response as JSON
            document.setAiAnalysisResult(
                    objectMapper.writeValueAsString(result)
            );

            document.setAiAnalysisStatus(AiAnalysisStatus.COMPLETED);
            document.setAiAnalysisCompletedAt(LocalDateTime.now());
            document.setAiAnalysisError(null);

            leaveDocumentRepository.save(document);

        } catch (Exception ex) {

        	log.error("AI analysis failed for document ID: {}",
        	        documentId, ex);
            // Record failure without affecting the leave decision
            try {
                leaveDocumentRepository.findById(documentId)
                        .ifPresent(document -> {

                            document.setAiAnalysisStatus(
                                    AiAnalysisStatus.FAILED
                            );

                            String message = ex.getMessage();

                            if (message == null || message.isBlank()) {
                                message = "Document analysis failed.";
                            }

                            if (message.length() > 1000) {
                                message = message.substring(0, 1000);
                            }

                            document.setAiAnalysisError(message);

                            leaveDocumentRepository.save(document);
                        });

            } catch (Exception persistenceException) {
                // Log this exception in production.
                // Do not change the leave approval decision.
            }
        }
    }
}

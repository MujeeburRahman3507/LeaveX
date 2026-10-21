package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.*;
import com.boit.boit_leave.AI.dto.DocumentAnalysisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Leave Request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "leave_request_id",
            nullable = false
    )
    private LeaveRequest leaveRequest;
    
    /*
     * Document Request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_request_id")
    private LeaveDocumentRequest documentRequest;

    /*
     * Document Type
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "document_type",
            nullable = false
    )
    private DocumentType documentType;

    /*
     * Document Status
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private DocumentStatus status;

    /*
     * Original file name
     * Example:
     * medical_certificate.pdf
     */
    @Column(
            name = "original_file_name",
            nullable = false
    )
    private String originalFileName;

    /*
     * Stored UUID file name
     * Example:
     * 7db54cf1-41ab-48b9.pdf
     */
    @Column(
            name = "stored_file_name",
            nullable = false
    )
    private String storedFileName;

    /*
     * Physical storage path
     */
    @Column(
            name = "file_path",
            nullable = false,
            length = 500
    )
    private String filePath;

    /*
     * File size (bytes)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /*
     * MIME Type
     * Example:
     * application/pdf
     * image/png
     */
    @Column(name = "content_type")
    private String contentType;

    /*
     * Employee remarks
     */
    @Column(name = "remarks")
    private String remarks;

    /*
     * Uploaded By
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uploaded_by",
            nullable = false
    )
    private User uploadedBy;

    /*
     * Upload Time
     */
    @Column(
            name = "uploaded_at",
            nullable = false
    )
    private LocalDateTime uploadedAt;
    
 // AI analysis processing status
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_analysis_status", nullable = false)
    @Builder.Default
    private AiAnalysisStatus aiAnalysisStatus = AiAnalysisStatus.PENDING;

    // Persist the complete AI response as JSON
    @Lob
    @Column(name = "ai_analysis_result")
    private String aiAnalysisResult;

    // Error details when analysis fails
    @Column(name = "ai_analysis_error", length = 1000)
    private String aiAnalysisError;

    // Time when analysis completed
    @Column(name = "ai_analysis_completed_at")
    private LocalDateTime aiAnalysisCompletedAt;

}
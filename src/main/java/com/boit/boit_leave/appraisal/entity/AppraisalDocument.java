package com.boit.boit_leave.appraisal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Appraisal to which this document belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "appraisal_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_appraisal_document_appraisal"
        )
    )
    private EmployeeAppraisal appraisal;

    /**
     * Optional goal association.
     *
     * If the document is related to a specific goal,
     * this field will contain that goal.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "goal_id",
        foreignKey = @ForeignKey(
            name = "fk_appraisal_document_goal"
        )
    )
    private AppraisalGoal goal;

    /**
     * Existing users.id of the person
     * who uploaded the document.
     */
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    /**
     * Original file name uploaded by the user.
     */
    @Column(
        name = "original_file_name",
        nullable = false,
        length = 500
    )
    private String originalFileName;

    /**
     * UUID/generated name used for physical storage.
     */
    @Column(
        name = "stored_file_name",
        length = 500
    )
    private String storedFileName;

    /**
     * Physical file path.
     */
    @Column(
        name = "file_path",
        nullable = false,
        length = 1000
    )
    private String filePath;

    /**
     * MIME type.
     *
     * Example:
     * application/pdf
     * image/png
     */
    @Column(
        name = "content_type",
        length = 150
    )
    private String contentType;

    /**
     * File size in bytes.
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Business document type.
     *
     * Example:
     * CERTIFICATE
     * PROJECT_EVIDENCE
     * TRAINING
     * OTHER
     */
    @Column(
        name = "document_type",
        length = 100
    )
    private String documentType;

    /**
     * Optional user remarks.
     */
    @Column(
        name = "remarks",
        columnDefinition = "TEXT"
    )
    private String remarks;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
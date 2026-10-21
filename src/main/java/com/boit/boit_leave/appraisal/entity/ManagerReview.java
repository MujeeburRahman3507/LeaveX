package com.boit.boit_leave.appraisal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "manager_review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_manager_review_appraisal",
            columnNames = "appraisal_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee appraisal associated with this manager review.
     *
     * One appraisal can have only one manager review.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "appraisal_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(
            name = "fk_manager_review_appraisal"
        )
    )
    private EmployeeAppraisal appraisal;

    /**
     * Manager's overall rating.
     *
     * Allowed range: 1.00 - 5.00
     */
    @Column(
        name = "overall_rating",
        precision = 3,
        scale = 2
    )
    private BigDecimal overallRating;

    /**
     * Manager's overall assessment/comments.
     */
    @Column(
        name = "overall_comments",
        columnDefinition = "TEXT"
    )
    private String overallComments;

    /**
     * Review status.
     *
     * DRAFT
     * SUBMITTED
     */
    @Column(
        nullable = false,
        length = 30
    )
    @Builder.Default
    private String status = "DRAFT";

    /**
     * Existing users.id of the manager
     * who submitted the review.
     */
    @Column(name = "submitted_by")
    private Long submittedBy;

    /**
     * Date/time when manager submitted
     * the review.
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
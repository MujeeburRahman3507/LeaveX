package com.boit.boit_leave.appraisal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "hr_review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_hr_review_appraisal",
            columnNames = "appraisal_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee appraisal associated with this HR review.
     *
     * One appraisal can have only one HR review.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "appraisal_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(
            name = "fk_hr_review_appraisal"
        )
    )
    private EmployeeAppraisal appraisal;

    /**
     * Final appraisal rating.
     *
     * Allowed range: 1.00 - 5.00
     */
    @Column(
        name = "final_rating",
        precision = 3,
        scale = 2
    )
    private BigDecimal finalRating;

    /**
     * Final performance category.
     *
     * Example:
     * Meets Expectations
     * Exceeds Expectations
     */
    @Column(
        name = "performance_category",
        length = 100
    )
    private String performanceCategory;

    /**
     * Final HR/Admin comments.
     */
    @Column(
        name = "comments",
        columnDefinition = "TEXT"
    )
    private String comments;

    /**
     * Review status.
     *
     * DRAFT
     * SUBMITTED
     * FINALIZED
     */
    @Column(
        nullable = false,
        length = 30
    )
    @Builder.Default
    private String status = "DRAFT";

    /**
     * Existing users.id of the ADMIN
     * who performed the HR review.
     */
    @Column(name = "submitted_by")
    private Long submittedBy;

    /**
     * Date/time when HR/Admin submitted
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
package com.boit.boit_leave.appraisal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "employee_review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_employee_review_appraisal",
            columnNames = "appraisal_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee appraisal associated with this self-review.
     *
     * One appraisal can have only one employee self-review.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "appraisal_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(
            name = "fk_employee_review_appraisal"
        )
    )
    private EmployeeAppraisal appraisal;

    /**
     * Employee's overall self-rating.
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
     * Employee's self-review comments.
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
     * Existing users.id of the employee
     * who submitted the review.
     */
    @Column(name = "submitted_by")
    private Long submittedBy;

    /**
     * Date/time when the employee submitted
     * the self-review.
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
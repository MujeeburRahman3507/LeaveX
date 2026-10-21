package com.boit.boit_leave.appraisal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Goal for which this progress update was created.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "goal_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_goal_progress_goal")
    )
    private AppraisalGoal goal;

    /**
     * Progress percentage.
     *
     * Example:
     * 20.00
     * 50.00
     * 100.00
     */
    @Column(
        name = "progress_percentage",
        nullable = false,
        precision = 5,
        scale = 2
    )
    @Builder.Default
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    /**
     * Current progress status.
     *
     * NOT_STARTED
     * IN_PROGRESS
     * COMPLETED
     * CANCELLED
     */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "IN_PROGRESS";

    /**
     * Employee's explanation/update.
     */
    @Column(name = "employee_comment", columnDefinition = "TEXT")
    private String employeeComment;

    /**
     * Existing users.id of the person
     * who submitted this progress update.
     */
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
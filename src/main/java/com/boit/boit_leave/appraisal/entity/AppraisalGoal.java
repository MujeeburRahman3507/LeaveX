package com.boit.boit_leave.appraisal.entity;

import com.boit.boit_leave.appraisal.enums.AppraisalGoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_goal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee appraisal to which this goal belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "appraisal_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_appraisal_goal_appraisal")
    )
    private EmployeeAppraisal appraisal;

    /**
     * Goal title.
     */
    @Column(nullable = false, length = 250)
    private String title;

    /**
     * Detailed goal description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Goal category.
     *
     * Example:
     * Project Delivery
     * Production Support
     * Professional Development
     */
    @Column(length = 100)
    private String category;

    /**
     * Expected target.
     */
    @Column(columnDefinition = "TEXT")
    private String target;

    /**
     * How achievement will be measured.
     */
    @Column(columnDefinition = "TEXT")
    private String measurement;

    /**
     * Goal importance.
     *
     * Example:
     * 40.00
     * 30.00
     * 30.00
     */
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Goal workflow status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AppraisalGoalStatus status = AppraisalGoalStatus.DRAFT;

    /**
     * Existing users.id of the employee who created the goal.
     */
    @Column(name = "created_by")
    private Long createdBy;

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
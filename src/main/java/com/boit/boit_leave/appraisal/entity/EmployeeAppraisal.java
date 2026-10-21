package com.boit.boit_leave.appraisal.entity;

import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "employee_appraisal",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_employee_appraisal_cycle_employee",
            columnNames = {"cycle_id", "employee_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAppraisal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Appraisal cycle.
     *
     * Example:
     * 2026 Annual Performance Appraisal
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "cycle_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_employee_appraisal_cycle")
    )
    private AppraisalCycle cycle;

    /**
     * Employee ID from existing users table.
     *
     * users.id = BIGINT
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * Manager ID from existing users table.
     *
     * users.id = BIGINT
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * Current appraisal workflow status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private AppraisalStatus status = AppraisalStatus.DRAFT;

    /**
     * Employee's overall self-review rating.
     */
    @Column(name = "overall_employee_rating", precision = 3, scale = 2)
    private BigDecimal overallEmployeeRating;

    /**
     * Manager's overall rating.
     */
    @Column(name = "overall_manager_rating", precision = 3, scale = 2)
    private BigDecimal overallManagerRating;

    /**
     * Final HR/Admin rating.
     */
    @Column(name = "final_rating", precision = 3, scale = 2)
    private BigDecimal finalRating;

    /**
     * Final performance category.
     *
     * Example:
     * Meets Expectations
     * Exceeds Expectations
     */
    @Column(name = "final_performance_category", length = 100)
    private String finalPerformanceCategory;

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
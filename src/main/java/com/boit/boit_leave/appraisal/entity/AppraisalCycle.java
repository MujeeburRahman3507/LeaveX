package com.boit.boit_leave.appraisal.entity;

import com.boit.boit_leave.appraisal.enums.AppraisalCycleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_cycle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "appraisal_start_date", nullable = false)
    private LocalDate appraisalStartDate;

    @Column(name = "appraisal_end_date", nullable = false)
    private LocalDate appraisalEndDate;

    @Column(name = "goal_setting_start_date")
    private LocalDate goalSettingStartDate;

    @Column(name = "goal_setting_end_date")
    private LocalDate goalSettingEndDate;

    @Column(name = "self_review_start_date")
    private LocalDate selfReviewStartDate;

    @Column(name = "self_review_end_date")
    private LocalDate selfReviewEndDate;

    @Column(name = "manager_review_start_date")
    private LocalDate managerReviewStartDate;

    @Column(name = "manager_review_end_date")
    private LocalDate managerReviewEndDate;

    @Column(name = "hr_review_start_date")
    private LocalDate hrReviewStartDate;

    @Column(name = "hr_review_end_date")
    private LocalDate hrReviewEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AppraisalCycleStatus status = AppraisalCycleStatus.DRAFT;

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
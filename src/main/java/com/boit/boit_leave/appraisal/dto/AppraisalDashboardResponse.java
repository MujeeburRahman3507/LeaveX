package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalDashboardResponse {

    private Long appraisalId;

    private Long cycleId;

    private String cycleName;

    private String appraisalStatus;

    private int totalGoals;

    private int approvedGoals;

    private int completedGoals;

    private BigDecimal totalGoalWeight;

    private BigDecimal overallProgress;

    private BigDecimal employeeRating;

    private BigDecimal managerRating;

    private BigDecimal finalRating;

    private String performanceCategory;

    private boolean selfReviewCompleted;

    private boolean managerReviewCompleted;

    private boolean hrReviewCompleted;
}
package com.boit.boit_leave.appraisal.dto;

import com.boit.boit_leave.appraisal.enums.AppraisalGoalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalGoalResponse {

    private Long id;

    private Long appraisalId;

    private String title;

    private String description;

    private String category;

    private String target;

    private String measurement;

    private BigDecimal weight;

    private LocalDate startDate;

    private LocalDate endDate;

    private AppraisalGoalStatus status;

    private Long createdBy;

    private BigDecimal progressPercentage;

    private String progressStatus;

    private String latestEmployeeComment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
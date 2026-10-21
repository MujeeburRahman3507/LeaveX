package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalProgressResponse {

    private Long id;

    private Long goalId;

    private BigDecimal progressPercentage;

    private String status;

    private String employeeComment;

    private Long updatedBy;

    private LocalDateTime updatedAt;
}
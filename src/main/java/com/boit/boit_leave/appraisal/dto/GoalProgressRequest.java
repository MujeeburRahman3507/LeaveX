package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalProgressRequest {

    private BigDecimal progressPercentage;

    private String status;

    private String employeeComment;
}
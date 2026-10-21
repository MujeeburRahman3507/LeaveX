package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeReviewRequest {

    private Long appraisalId;

    private BigDecimal overallRating;

    private String overallComments;
}
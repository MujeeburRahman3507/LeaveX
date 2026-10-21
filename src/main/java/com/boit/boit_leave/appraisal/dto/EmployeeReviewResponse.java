package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeReviewResponse {

    private Long id;

    private Long appraisalId;

    private BigDecimal overallRating;

    private String overallComments;

    private String status;

    private Long submittedBy;

    private LocalDateTime submittedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrReviewResponse {

    private Long id;

    private Long appraisalId;

    private BigDecimal finalRating;

    private String performanceCategory;

    private String comments;

    private String status;

    private Long submittedBy;

    private LocalDateTime submittedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
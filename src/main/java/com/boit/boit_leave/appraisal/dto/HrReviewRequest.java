package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrReviewRequest {

    private Long appraisalId;

    private BigDecimal finalRating;

    private String performanceCategory;

    private String comments;
}
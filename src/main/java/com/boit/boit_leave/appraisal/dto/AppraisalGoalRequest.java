package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalGoalRequest {

    private Long appraisalId;

    private String title;

    private String description;

    private String category;

    private String target;

    private String measurement;

    private BigDecimal weight;

    private LocalDate startDate;

    private LocalDate endDate;
}
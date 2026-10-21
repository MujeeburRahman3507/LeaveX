package com.boit.boit_leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDto {

    private BigDecimal annualBalance;

    private BigDecimal usedLeave;

    private BigDecimal remainingLeave;

    private Long approvedLeaves;

    private Long rejectedLeaves;

    private Long pendingLeaves;

    private BigDecimal casualLeave;

    private BigDecimal medicalLeave;

    private BigDecimal earnedLeave;
}
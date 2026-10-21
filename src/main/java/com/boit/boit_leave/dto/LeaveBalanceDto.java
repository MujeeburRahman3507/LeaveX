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
public class LeaveBalanceDto {

    private BigDecimal annualBalance;

    private BigDecimal usedLeave;

    private BigDecimal remainingLeave;

    private Long approvedCount;

    private Long rejectedCount;

    private Long pendingCount;
}
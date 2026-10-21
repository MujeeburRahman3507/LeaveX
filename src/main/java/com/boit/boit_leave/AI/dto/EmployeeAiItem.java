package com.boit.boit_leave.AI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAiItem {

    private Long requestId;

    private String leaveType;

    private String startDate;

    private String endDate;

    private BigDecimal totalDays;

    private String status;

    private String reason;

    private String documentStatus;

    private Boolean documentRequested;

}
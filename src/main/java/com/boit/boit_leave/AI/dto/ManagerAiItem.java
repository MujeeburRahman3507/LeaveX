package com.boit.boit_leave.AI.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ManagerAiItem {

    private Long requestId;

    private String employeeName;

    private String leaveType;

    private String startDate;

    private String endDate;

    private BigDecimal totalDays;

    private String status;

    private String reason;

}
package com.boit.boit_leave.AI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAiItem {

    private Long id;

    private String employeeName;
    
    private Long requestId;

    private String employeeCode;

    private String email;
    
    private Integer count;

    private String country;

    private String leaveType;

    private String startDate;

    private String endDate;

    private Integer totalDays;

    private String status;

    private String reason;

    private String department;

    private String holidayName;

    private String holidayDate;
    
 // SLA information
    private String slaStatus;
    private String slaDueAt;
    private Integer currentApprovalLevel;
    private String approverName;
}
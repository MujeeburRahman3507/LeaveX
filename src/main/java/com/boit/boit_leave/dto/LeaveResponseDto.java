package com.boit.boit_leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.boit.boit_leave.entity.DocumentPriority;
import com.boit.boit_leave.entity.DocumentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponseDto {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private String employeeEmail;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalDays;

    private String reason;

    private String status;

    private Long approverId;

    private String approverName;

    private String managerComments;

    private LocalDateTime actionDate;

    private LocalDateTime createdAt;

    /* SLA Information */

    private String slaStatus;

    private LocalDateTime slaDueAt;

    //

    private DocumentStatus documentStatus;

    private DocumentPriority documentPriority;

    private Boolean documentRequested;

    private String durationType;
    
    private Long backupPersonId;
    private String backupPersonName;
}
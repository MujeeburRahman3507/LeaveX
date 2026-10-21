package com.boit.boit_leave.appraisal.dto;

import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAppraisalResponse {

    private Long id;

    private Long cycleId;

    private String cycleName;

    private Long employeeId;

    private String employeeName;

    private Long managerId;

    private String managerName;

    private AppraisalStatus status;

    private BigDecimal overallEmployeeRating;

    private BigDecimal overallManagerRating;

    private BigDecimal finalRating;

    private String finalPerformanceCategory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
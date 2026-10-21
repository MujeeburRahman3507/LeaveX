package com.boit.boit_leave.dto;

import com.boit.boit_leave.entity.LeaveDurationType;
import com.boit.boit_leave.entity.LeaveType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Leave duration is required")
    @Builder.Default
    private LeaveDurationType durationType =
            LeaveDurationType.FULL_DAY;

    @NotBlank(message = "Reason is required")
    private String reason;
    
    private Long backupPersonId;
}
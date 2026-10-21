package com.boit.boit_leave.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveConflictResponseDto {

    private boolean conflict;

    private long employeesOnLeave;

    private String department;

    private String recommendation;
}
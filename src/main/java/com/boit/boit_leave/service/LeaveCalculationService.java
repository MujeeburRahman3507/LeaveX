package com.boit.boit_leave.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.boit.boit_leave.dto.LeaveCalculationResponseDto;
import com.boit.boit_leave.entity.LeaveDurationType;

public interface LeaveCalculationService {

    BigDecimal calculateLeaveDays(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveDurationType duration
    );

    LeaveCalculationResponseDto getLeaveCalculationDetails(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveDurationType duration
    );
}
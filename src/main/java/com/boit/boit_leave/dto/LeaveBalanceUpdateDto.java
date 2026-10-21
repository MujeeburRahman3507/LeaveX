package com.boit.boit_leave.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceUpdateDto {

    @NotNull(message = "Annual balance is required")
    @DecimalMin(value = "0.0", message = "Annual balance cannot be negative")
    private BigDecimal annualBalance;
}
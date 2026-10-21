package com.boit.boit_leave.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
public class LeaveCalculationResponseDto {

	private BigDecimal totalLeaveDays;

    private List<String> excludedDays;

	public BigDecimal getTotalLeaveDays() {
		return totalLeaveDays;
	}

	public void setTotalLeaveDays(BigDecimal totalLeaveDays) {
		this.totalLeaveDays = totalLeaveDays;
	}

	public List<String> getExcludedDays() {
		return excludedDays;
	}

	public void setExcludedDays(List<String> excludedDays) {
		this.excludedDays = excludedDays;
	}
    
    
}
package com.boit.boit_leave.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {

    private Long totalEmployees;

    private Long activeEmployees;

    private Long employeesOnLeaveToday;

    private Long pendingApprovals;

    private Long totalCountries;

    private Long totalPolicies;

    private Long upcomingHolidays;

	public Long getTotalEmployees() {
		return totalEmployees;
	}

	public void setTotalEmployees(Long totalEmployees) {
		this.totalEmployees = totalEmployees;
	}

	public Long getActiveEmployees() {
		return activeEmployees;
	}

	public void setActiveEmployees(Long activeEmployees) {
		this.activeEmployees = activeEmployees;
	}

	public Long getEmployeesOnLeaveToday() {
		return employeesOnLeaveToday;
	}

	public void setEmployeesOnLeaveToday(Long employeesOnLeaveToday) {
		this.employeesOnLeaveToday = employeesOnLeaveToday;
	}

	public Long getPendingApprovals() {
		return pendingApprovals;
	}

	public void setPendingApprovals(Long pendingApprovals) {
		this.pendingApprovals = pendingApprovals;
	}

	public Long getTotalCountries() {
		return totalCountries;
	}

	public void setTotalCountries(Long totalCountries) {
		this.totalCountries = totalCountries;
	}

	public Long getTotalPolicies() {
		return totalPolicies;
	}

	public void setTotalPolicies(Long totalPolicies) {
		this.totalPolicies = totalPolicies;
	}

	public Long getUpcomingHolidays() {
		return upcomingHolidays;
	}

	public void setUpcomingHolidays(Long upcomingHolidays) {
		this.upcomingHolidays = upcomingHolidays;
	}
    
    
}
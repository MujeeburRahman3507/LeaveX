package com.boit.boit_leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayGreetingDto {

    private boolean birthday;

    private String employeeName;

    private String message;

	public boolean isBirthday() {
		return birthday;
	}

	public void setBirthday(boolean birthday) {
		this.birthday = birthday;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

    
}
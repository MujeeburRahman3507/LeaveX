package com.boit.boit_leave.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequestDto {

    private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
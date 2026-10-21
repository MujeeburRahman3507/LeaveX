package com.boit.boit_leave.dto;

import com.boit.boit_leave.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;

    private String employeeCode;

    private String fullName;

    private String email;

    private Role role;

    private Long managerId;

    private Boolean active;
    
    private Long leavePolicyId;
    
    private String leavePolicyName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getLeavePolicyId() {
		return leavePolicyId;
	}

	public void setLeavePolicyId(Long leavePolicyId) {
		this.leavePolicyId = leavePolicyId;
	}

	public String getLeavePolicyName() {
		return leavePolicyName;
	}

	public void setLeavePolicyName(String leavePolicyName) {
		this.leavePolicyName = leavePolicyName;
	}
    
    
}
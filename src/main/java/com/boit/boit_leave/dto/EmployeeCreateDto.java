package com.boit.boit_leave.dto;

import java.time.LocalDate;
import java.util.List;

import com.boit.boit_leave.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDto {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private Long managerId;
    
    private String department;
    
    private List<Long> countryIds;
    
    private Long leavePolicyId;
    
    private LocalDate dateOfBirth;
    
    private Integer approvalLevels;
    
    private Long level1ApproverId;
    
    public Integer getApprovalLevels() {
		return approvalLevels;
	}

	public void setApprovalLevels(Integer approvalLevels) {
		this.approvalLevels = approvalLevels;
	}

	public Long getLevel1ApproverId() {
		return level1ApproverId;
	}

	public void setLevel1ApproverId(Long level1ApproverId) {
		this.level1ApproverId = level1ApproverId;
	}

	public Long getLevel2ApproverId() {
		return level2ApproverId;
	}

	public void setLevel2ApproverId(Long level2ApproverId) {
		this.level2ApproverId = level2ApproverId;
	}

	private Long level2ApproverId;

    
    

    public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Long getLeavePolicyId() {
		return leavePolicyId;
	}

	public void setLeavePolicyId(Long leavePolicyId) {
		this.leavePolicyId = leavePolicyId;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@Builder.Default
    private Boolean active = true;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public List<Long> getCountryIds() {
		return countryIds;
	}

	public void setCountryIds(List<Long> countryIds) {
		this.countryIds = countryIds;
	}
	
	
}
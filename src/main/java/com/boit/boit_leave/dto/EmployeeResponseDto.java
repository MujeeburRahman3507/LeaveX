package com.boit.boit_leave.dto;

import java.time.LocalDate;
import java.util.List;

import com.boit.boit_leave.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {

    private Long id;

    private String employeeCode;

    private String fullName;

    private String email;

    private Role role;
    
    private String department;
    
    private List<Long> countryIds;

    private List<String> countryNames;
    
    private Long leavePolicyId;
    
    private String leavePolicyName;
    
    private LocalDate dateOfBirth;
    

private Integer approvalLevels;

private Long level1ApproverId;
private String level1ApproverName;

private Long level2ApproverId;
private String level2ApproverName;


    

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

public String getLevel1ApproverName() {
	return level1ApproverName;
}

public void setLevel1ApproverName(String level1ApproverName) {
	this.level1ApproverName = level1ApproverName;
}

public Long getLevel2ApproverId() {
	return level2ApproverId;
}

public void setLevel2ApproverId(Long level2ApproverId) {
	this.level2ApproverId = level2ApproverId;
}

public String getLevel2ApproverName() {
	return level2ApproverName;
}

public void setLevel2ApproverName(String level2ApproverName) {
	this.level2ApproverName = level2ApproverName;
}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getLeavePolicyName() {
		return leavePolicyName;
	}

	public void setLeavePolicyName(String leavePolicyName) {
		this.leavePolicyName = leavePolicyName;
	}

	public Long getLeavePolicyId() {
		return leavePolicyId;
	}

	public void setLeavePolicyId(Long leavePolicyId) {
		this.leavePolicyId = leavePolicyId;
	}

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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	
	

	public List<Long> getCountryIds() {
		return countryIds;
	}

	public void setCountryIds(List<Long> countryIds) {
		this.countryIds = countryIds;
	}

	public List<String> getCountryNames() {
		return countryNames;
	}

	public void setCountryNames(List<String> countryNames) {
		this.countryNames = countryNames;
	}



	private Boolean active;

    private Long managerId;

    private String managerName;
}
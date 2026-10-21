package com.boit.boit_leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyDto {

    private Long id;

    private String policyName;

    private Integer annualLeave;

    private Integer medicalLeave;

    private Integer casualLeave;

    private Integer earnedLeave;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public Integer getAnnualLeave() {
		return annualLeave;
	}

	public void setAnnualLeave(Integer annualLeave) {
		this.annualLeave = annualLeave;
	}

	public Integer getMedicalLeave() {
		return medicalLeave;
	}

	public void setMedicalLeave(Integer medicalLeave) {
		this.medicalLeave = medicalLeave;
	}

	public Integer getCasualLeave() {
		return casualLeave;
	}

	public void setCasualLeave(Integer casualLeave) {
		this.casualLeave = casualLeave;
	}

	public Integer getEarnedLeave() {
		return earnedLeave;
	}

	public void setEarnedLeave(Integer earnedLeave) {
		this.earnedLeave = earnedLeave;
	}
    
    
}

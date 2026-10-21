package com.boit.boit_leave.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicy {
	

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String policyName;

	    private Integer annualLeave;

	    private Integer medicalLeave;

	    private Integer casualLeave;

	    private Integer earnedLeave;

	    private LocalDateTime createdAt;

	    private LocalDateTime updatedAt;

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

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}

		public LocalDateTime getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
		}
	    
	    
	}



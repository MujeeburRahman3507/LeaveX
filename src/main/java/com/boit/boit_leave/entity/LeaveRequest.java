package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.boit.boit_leave.entity.LeaveDurationType;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_person_id")
    private User backupPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType leaveType;
    


    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LeaveType getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public BigDecimal  getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(BigDecimal  totalDays) {
		this.totalDays = totalDays;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LeaveStatus getStatus() {
		return status;
	}

	public void setStatus(LeaveStatus status) {
		this.status = status;
	}

	public User getApprover() {
		return approver;
	}

	public void setApprover(User approver) {
		this.approver = approver;
	}

	public String getManagerComments() {
		return managerComments;
	}

	public void setManagerComments(String managerComments) {
		this.managerComments = managerComments;
	}

	public LocalDateTime getActionDate() {
		return actionDate;
	}

	public void setActionDate(LocalDateTime actionDate) {
		this.actionDate = actionDate;
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

	@Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

//    @Column(name = "total_days", nullable = false)
//    private Integer totalDays;
    
    @Column(name = "total_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalDays;
    
    //newly added for half day leave implementation//
    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false)
    private LeaveDurationType durationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approver;
    

@Column(name = "current_approval_level", nullable = false)
@Builder.Default
private Integer currentApprovalLevel = 1;


    @Column(name = "manager_comments")
    private String managerComments;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    public User getBackupPerson() {
		return backupPerson;
	}

	public void setBackupPerson(User backupPerson) {
		this.backupPerson = backupPerson;
	}

	public LeaveDurationType getDurationType() {
		return durationType;
	}

	public void setDurationType(LeaveDurationType durationType) {
		this.durationType = durationType;
	}

	public Integer getCurrentApprovalLevel() {
		return currentApprovalLevel;
	}

	public void setCurrentApprovalLevel(Integer currentApprovalLevel) {
		this.currentApprovalLevel = currentApprovalLevel;
	}

	public LocalDateTime getSlaDueAt() {
		return slaDueAt;
	}

	public void setSlaDueAt(LocalDateTime slaDueAt) {
		this.slaDueAt = slaDueAt;
	}

	public Boolean getSlaWarningSent() {
		return slaWarningSent;
	}

	public void setSlaWarningSent(Boolean slaWarningSent) {
		this.slaWarningSent = slaWarningSent;
	}

	public Boolean getSlaBreachEmailSent() {
		return slaBreachEmailSent;
	}

	public void setSlaBreachEmailSent(Boolean slaBreachEmailSent) {
		this.slaBreachEmailSent = slaBreachEmailSent;
	}

	public SlaStatus getSlaStatus() {
		return slaStatus;
	}

	public void setSlaStatus(SlaStatus slaStatus) {
		this.slaStatus = slaStatus;
	}

	@Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "sla_due_at")
    private LocalDateTime slaDueAt;

    @Column(name = "sla_warning_sent", nullable = false)
    @Builder.Default
    private Boolean slaWarningSent = false;

    @Column(name = "sla_breach_email_sent", nullable = false)
    @Builder.Default
    private Boolean slaBreachEmailSent = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_status", nullable = false)
    @Builder.Default
    private SlaStatus slaStatus = SlaStatus.WITHIN_SLA;

}
package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

//    @Column(name = "annual_balance", nullable = false)
//    private Integer annualBalance;
//
//    @Column(name = "used_leave", nullable = false)
//    private Integer usedLeave;
//
//    @Column(name = "remaining_leave", nullable = false)
//    private Integer remainingLeave;
    
    @Column(name = "annual_balance", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualBalance;

    @Column(name = "used_leave", nullable = false, precision = 5, scale = 2)
    private BigDecimal usedLeave;

    @Column(name = "remaining_leave", nullable = false, precision = 5, scale = 2)
    private BigDecimal remainingLeave;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

	public BigDecimal getAnnualBalance() {
	    return annualBalance;
	}

	public void setAnnualBalance(BigDecimal annualBalance) {
	    this.annualBalance = annualBalance;
	}

	public BigDecimal getUsedLeave() {
		return usedLeave;
	}

	public void setUsedLeave(BigDecimal usedLeave) {
		this.usedLeave = usedLeave;
	}

	public BigDecimal getRemainingLeave() {
		return remainingLeave;
	}

	public void setRemainingLeave(BigDecimal remainingLeave) {
		this.remainingLeave = remainingLeave;
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

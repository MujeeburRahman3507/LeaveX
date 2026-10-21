package com.boit.boit_leave.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(
            LeaveStatus status
    );

    List<LeaveRequest> findByUserManagerIdAndStatusOrderByCreatedAtDesc(
            Long managerId,
            LeaveStatus status
    );

    List<LeaveRequest> findByUserManagerIdOrderByCreatedAtDesc(
            Long managerId
    );

    long countByStatus(
            LeaveStatus status
    );
    
    boolean existsByUserIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId,
            List<LeaveStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
    
    List<LeaveRequest> findByUserManagerIdOrderByStartDateAsc(
            Long managerId
    );
    
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
    
    @Query("""
    		SELECT COUNT(l)
    		FROM LeaveRequest l
    		WHERE l.user.department = :department
    		AND l.status = com.boit.boit_leave.entity.LeaveStatus.APPROVED
    		AND l.startDate <= :endDate
    		AND l.endDate >= :startDate
    		""")
    		long countDepartmentConflicts(
    		        String department,
    		        LocalDate startDate,
    		        LocalDate endDate
    		);
    
    @Query("""
    		SELECT COUNT(lr)
    		FROM LeaveRequest lr
    		WHERE lr.user.department = :department
    		AND lr.status = com.boit.boit_leave.entity.LeaveStatus.APPROVED
    		AND lr.startDate <= :endDate
    		AND lr.endDate >= :startDate
    		""")
    		long countConflictingLeaves(
    		        @Param("department") String department,
    		        @Param("startDate") LocalDate startDate,
    		        @Param("endDate") LocalDate endDate
    		);
    
    @Query("""
    		SELECT lr
    		FROM LeaveRequest lr
    		WHERE lr.status = 'APPROVED'
    		AND CURRENT_DATE BETWEEN lr.startDate AND lr.endDate
    		""")
    		List<LeaveRequest> findEmployeesOnLeaveToday();
    
    List<LeaveRequest> findByStatus(
            LeaveStatus status
    );
    
    @Query("""
    		SELECT COUNT(DISTINCT lr.user.id)
    		FROM LeaveRequest lr
    		WHERE lr.status = com.boit.boit_leave.entity.LeaveStatus.APPROVED
    		AND :today BETWEEN lr.startDate AND lr.endDate
    		""")
    		Long countEmployeesOnLeaveToday(
    		        @Param("today") LocalDate today
    		);
    
    List<LeaveRequest>
    findByUserIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId,
            List<LeaveStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
    
    
    List<LeaveRequest>
    findByStatusAndSlaDueAtIsNotNullAndSlaWarningSentFalse(
            LeaveStatus status
    );

    @Query(value = """
    	    SELECT *
    	    FROM leave_requests
    	    WHERE status = 'PENDING'
    	      AND sla_due_at IS NOT NULL
    	      AND sla_due_at < CURRENT_TIMESTAMP
    	      AND sla_breach_email_sent = FALSE
    	    """, nativeQuery = true)
    	List<LeaveRequest> findBreachedSlaRequests(
    	        @Param("now") LocalDateTime now
    	);

    	@Query(value = """
    	    SELECT COUNT(*)
    	    FROM leave_requests
    	    WHERE status = 'PENDING'
    	      AND sla_due_at IS NOT NULL
    	      AND sla_due_at < CURRENT_TIMESTAMP
    	      AND sla_breach_email_sent = FALSE
    	    """, nativeQuery = true)
    	long countBreachedSlaRequests();
    	

    	//2 APPROVAL FLOW//
    	

@Query("""
    SELECT lr
    FROM LeaveRequest lr
    JOIN lr.user employee
    WHERE lr.status = :status
      AND (
          (
              lr.currentApprovalLevel = 1
              AND employee.level1Approver.id = :approverId
          )
          OR
          (
              lr.currentApprovalLevel = 2
              AND employee.approvalLevels = 2
              AND employee.level2Approver.id = :approverId
          )
      )
    ORDER BY lr.createdAt DESC
    """)
List<LeaveRequest> findPendingRequestsForApprover(
        @Param("approverId") Long approverId,
        @Param("status") LeaveStatus status
);


}

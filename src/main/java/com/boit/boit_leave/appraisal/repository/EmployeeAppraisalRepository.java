package com.boit.boit_leave.appraisal.repository;

import com.boit.boit_leave.appraisal.entity.EmployeeAppraisal;
import com.boit.boit_leave.appraisal.enums.AppraisalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EmployeeAppraisalRepository extends JpaRepository<EmployeeAppraisal, Long> {

    Optional<EmployeeAppraisal> findByCycleIdAndEmployeeId(
            Long cycleId,
            Long employeeId
    );

    List<EmployeeAppraisal> findByEmployeeIdOrderByCreatedAtDesc(
            Long employeeId
    );

    List<EmployeeAppraisal> findByManagerIdOrderByCreatedAtDesc(
            Long managerId
    );

    List<EmployeeAppraisal> findByManagerIdAndStatusOrderByCreatedAtDesc(
            Long managerId,
            AppraisalStatus status
    );

    List<EmployeeAppraisal> findByStatusOrderByCreatedAtDesc(
            AppraisalStatus status
    );
    
    @Query("""
    	    SELECT a
    	    FROM EmployeeAppraisal a
    	    LEFT JOIN FETCH a.cycle
    	    """)
    	List<EmployeeAppraisal> findAllWithCycle();
}
package com.boit.boit_leave.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boit.boit_leave.entity.LeavePolicy;

@Repository
public interface LeavePolicyRepository
        extends JpaRepository<LeavePolicy, Long> {
}

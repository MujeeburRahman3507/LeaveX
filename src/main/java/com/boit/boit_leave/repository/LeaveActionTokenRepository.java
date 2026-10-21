package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.LeaveActionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveActionTokenRepository
        extends JpaRepository<LeaveActionToken, Long> {

    Optional<LeaveActionToken> findByTokenHashAndUsedFalse(
            String tokenHash
    );

    List<LeaveActionToken> findByLeaveRequestIdAndManagerIdAndUsedFalse(
            Long leaveRequestId,
            Long managerId
    );
}

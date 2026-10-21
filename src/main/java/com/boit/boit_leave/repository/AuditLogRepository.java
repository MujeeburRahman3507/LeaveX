package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActionByIdOrderByCreatedAtDesc(
            Long userId
    );

    List<AuditLog> findAllByOrderByCreatedAtDesc();
    
    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtAsc(
            String entityName,
            Long entityId
    );

}

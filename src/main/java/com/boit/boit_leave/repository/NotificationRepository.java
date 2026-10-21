package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    long countByUserIdAndIsReadFalse(
            Long userId
    );
    
    List<Notification>
    findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
            Long userId
    );
    
    List<Notification>
    findByUserIdAndIsReadFalse(
            Long userId
    );
}
package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository
        extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateCodeAndActiveTrue(
            String templateCode
    );

    Optional<NotificationTemplate> findByTemplateCode(
            String templateCode
    );
}
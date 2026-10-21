package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.BackdatedLeaveConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackdatedLeaveConfigurationRepository
        extends JpaRepository<BackdatedLeaveConfiguration, Long> {

    Optional<BackdatedLeaveConfiguration>
    findFirstByActiveTrue();
}
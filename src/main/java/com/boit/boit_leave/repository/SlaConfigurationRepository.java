package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.SlaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaConfigurationRepository
        extends JpaRepository<SlaConfiguration, Long> {

    Optional<SlaConfiguration> findFirstByActiveTrue();
}

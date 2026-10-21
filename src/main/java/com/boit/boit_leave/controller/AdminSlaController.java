package com.boit.boit_leave.controller;


import com.boit.boit_leave.dto.SlaConfigurationDto;
import com.boit.boit_leave.service.SlaConfigurationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sla")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSlaController {

    private final SlaConfigurationService slaConfigurationService;


    /**
     * Get active SLA configuration.
     */
    @GetMapping
    public ResponseEntity<SlaConfigurationDto> getConfiguration() {

        return ResponseEntity.ok(
                slaConfigurationService.getConfiguration()
        );
    }


    /**
     * Save/update SLA configuration.
     */
    @PutMapping
    public ResponseEntity<SlaConfigurationDto> saveConfiguration(
            @RequestBody SlaConfigurationDto dto) {

        return ResponseEntity.ok(
                slaConfigurationService.saveConfiguration(dto)
        );
    }
}

package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.BackdatedLeaveConfigurationDto;
import com.boit.boit_leave.service.BackdatedLeaveConfigurationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/backdated-leave")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBackdatedLeaveController {

    private final BackdatedLeaveConfigurationService service;

    @GetMapping
    public BackdatedLeaveConfigurationDto getConfiguration() {

        return service.getConfiguration();
    }

    @PutMapping
    public BackdatedLeaveConfigurationDto updateConfiguration(
            @RequestBody BackdatedLeaveConfigurationDto dto
    ) {

        return service.updateConfiguration(dto);
    }
}
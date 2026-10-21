package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.BackdatedLeaveConfigurationDto;

public interface BackdatedLeaveConfigurationService {

    BackdatedLeaveConfigurationDto getConfiguration();

    BackdatedLeaveConfigurationDto updateConfiguration(
            BackdatedLeaveConfigurationDto dto
    );
}
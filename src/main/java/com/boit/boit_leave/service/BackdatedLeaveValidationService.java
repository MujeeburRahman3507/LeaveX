package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.BackdatedLeaveConfigurationDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BackdatedLeaveValidationService {

    private final BackdatedLeaveConfigurationService
            configurationService;

    public void validateBackdatedLeave(
            String leaveType,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate applicationDate
    ) {

        if (
                startDate == null
                ||
                endDate == null
        ) {

            return;
        }

        /*
         * Future/current leave does not need
         * backdated validation.
         */
        if (
                !endDate.isBefore(applicationDate)
        ) {

            return;
        }

        /*
         * Only Medical Leave can be
         * applied retrospectively.
         */
        if (
                leaveType == null
                ||
                !leaveType.trim()
                        .equalsIgnoreCase("MEDICAL")
        ) {

            throw new IllegalArgumentException(
                    "Backdated leave is allowed only for Medical Leave."
            );
        }

        BackdatedLeaveConfigurationDto config =
                configurationService.getConfiguration();

        long daysSinceLeaveEnd =
                ChronoUnit.DAYS.between(
                        endDate,
                        applicationDate
                );

        if (
                daysSinceLeaveEnd
                >
                config.getMaxBackdatedDays()
        ) {

            throw new IllegalArgumentException(
                    "Medical leave can only be applied within "
                    + config.getMaxBackdatedDays()
                    + " days from the leave end date."
            );
        }
    }
}
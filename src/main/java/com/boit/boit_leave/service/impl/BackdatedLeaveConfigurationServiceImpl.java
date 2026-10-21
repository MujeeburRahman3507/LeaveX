package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.BackdatedLeaveConfigurationDto;
import com.boit.boit_leave.entity.BackdatedLeaveConfiguration;
import com.boit.boit_leave.repository.BackdatedLeaveConfigurationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackdatedLeaveConfigurationServiceImpl
        implements BackdatedLeaveConfigurationService {

    private final BackdatedLeaveConfigurationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public BackdatedLeaveConfigurationDto getConfiguration() {

        BackdatedLeaveConfiguration config =
                repository
                        .findFirstByActiveTrue()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Backdated leave configuration not found."
                                )
                        );

        return mapToDto(config);
    }

    @Override
    @Transactional
    public BackdatedLeaveConfigurationDto updateConfiguration(
            BackdatedLeaveConfigurationDto dto
    ) {

        if (dto == null) {

            throw new IllegalArgumentException(
                    "Configuration is required."
            );
        }

        if (
                dto.getMaxBackdatedDays() == null
                ||
                dto.getMaxBackdatedDays() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Maximum backdated days must be greater than 0."
            );
        }

        if (
                dto.getMedicalDocumentRequired() == null
        ) {

            throw new IllegalArgumentException(
                    "Medical document requirement must be specified."
            );
        }

        BackdatedLeaveConfiguration config =
                repository
                        .findFirstByActiveTrue()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Backdated leave configuration not found."
                                )
                        );

        config.setMaxBackdatedDays(
                dto.getMaxBackdatedDays()
        );

        config.setMedicalDocumentRequired(
                dto.getMedicalDocumentRequired()
        );

        return mapToDto(
                repository.save(config)
        );
    }

    private BackdatedLeaveConfigurationDto mapToDto(
            BackdatedLeaveConfiguration config
    ) {

        return BackdatedLeaveConfigurationDto.builder()

                .id(config.getId())

                .maxBackdatedDays(
                        config.getMaxBackdatedDays()
                )

                .medicalDocumentRequired(
                        config.getMedicalDocumentRequired()
                )

                .active(
                        config.getActive()
                )

                .build();
    }
}
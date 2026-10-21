package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.SlaConfiguration;
import com.boit.boit_leave.dto.SlaConfigurationDto;
import com.boit.boit_leave.repository.SlaConfigurationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SlaConfigurationService {

    private final SlaConfigurationRepository slaConfigurationRepository;


    /**
     * Get the currently active SLA configuration.
     */
    @Transactional(readOnly = true)
    public SlaConfigurationDto getConfiguration() {

        SlaConfiguration configuration =
                slaConfigurationRepository
                        .findFirstByActiveTrue()
                        .orElseGet(this::createDefaultConfiguration);

        return toDto(configuration);
    }


    /**
     * Save/update the active SLA configuration.
     */
    @Transactional
    public SlaConfigurationDto saveConfiguration(
            SlaConfigurationDto dto) {

        validateConfiguration(dto);

        SlaConfiguration configuration =
                slaConfigurationRepository
                        .findFirstByActiveTrue()
                        .orElseGet(SlaConfiguration::new);

        configuration.setSlaDuration(dto.getSlaDuration());
        configuration.setSlaUnit(dto.getSlaUnit());
        configuration.setCalculationType(
                dto.getCalculationType()
        );
        configuration.setWarningBefore(
                dto.getWarningBefore()
        );
        configuration.setTokenExpirySameAsSla(
                dto.getTokenExpirySameAsSla()
        );
        configuration.setActive(true);

        SlaConfiguration saved =
                slaConfigurationRepository.save(configuration);

        return toDto(saved);
    }


    /**
     * Validate SLA configuration.
     */
    private void validateConfiguration(
            SlaConfigurationDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "SLA configuration is required"
            );
        }

        if (dto.getSlaDuration() == null ||
                dto.getSlaDuration() <= 0) {

            throw new IllegalArgumentException(
                    "SLA duration must be greater than zero"
            );
        }

        if (dto.getSlaUnit() == null ||
                dto.getSlaUnit().isBlank()) {

            throw new IllegalArgumentException(
                    "SLA unit is required"
            );
        }

        if (!"DAYS".equalsIgnoreCase(dto.getSlaUnit()) &&
                !"HOURS".equalsIgnoreCase(dto.getSlaUnit())) {

            throw new IllegalArgumentException(
                    "SLA unit must be DAYS or HOURS"
            );
        }

        if (dto.getCalculationType() == null ||
                dto.getCalculationType().isBlank()) {

            throw new IllegalArgumentException(
                    "Calculation type is required"
            );
        }

        if (!"WORKING_DAYS".equalsIgnoreCase(
                dto.getCalculationType()) &&
                !"CALENDAR_DAYS".equalsIgnoreCase(
                        dto.getCalculationType())) {

            throw new IllegalArgumentException(
                    "Calculation type must be WORKING_DAYS or CALENDAR_DAYS"
            );
        }

        if (dto.getWarningBefore() == null ||
                dto.getWarningBefore() < 0) {

            throw new IllegalArgumentException(
                    "Warning period cannot be negative"
            );
        }

        if ("DAYS".equalsIgnoreCase(dto.getSlaUnit()) &&
                dto.getWarningBefore() >= dto.getSlaDuration()) {

            throw new IllegalArgumentException(
                    "Warning period must be less than SLA duration"
            );
        }
    }


    /**
     * Create default configuration if none exists.
     */
    private SlaConfiguration createDefaultConfiguration() {

        SlaConfiguration configuration =
                SlaConfiguration.builder()
                        .slaDuration(3)
                        .slaUnit("DAYS")
                        .calculationType("WORKING_DAYS")
                        .warningBefore(1)
                        .tokenExpirySameAsSla(true)
                        .active(true)
                        .build();

        return slaConfigurationRepository.save(
                configuration
        );
    }


    /**
     * Entity -> DTO.
     */
    private SlaConfigurationDto toDto(
            SlaConfiguration configuration) {

        return SlaConfigurationDto.builder()
                .id(configuration.getId())
                .slaDuration(
                        configuration.getSlaDuration()
                )
                .slaUnit(
                        configuration.getSlaUnit()
                )
                .calculationType(
                        configuration.getCalculationType()
                )
                .warningBefore(
                        configuration.getWarningBefore()
                )
                .tokenExpirySameAsSla(
                        configuration.getTokenExpirySameAsSla()
                )
                .active(
                        configuration.getActive()
                )
                .build();
    }
}

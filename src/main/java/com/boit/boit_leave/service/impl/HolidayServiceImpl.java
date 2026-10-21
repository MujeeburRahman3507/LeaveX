package com.boit.boit_leave.service.impl;

import com.boit.boit_leave.dto.HolidayDto;
import com.boit.boit_leave.entity.Country;
import com.boit.boit_leave.entity.Holiday;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.CountryRepository;
import com.boit.boit_leave.repository.HolidayRepository;
import com.boit.boit_leave.repository.UserCountryMappingRepository;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayServiceImpl
        implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;

    private final UserCountryMappingRepository
            userCountryMappingRepository;

    @Override
    public List<HolidayDto> getAll() {

        return holidayRepository
                .findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public HolidayDto create(
            HolidayDto dto) {

        Country country = null;

        if (dto.getCountryId() != null) {

            country =
                    countryRepository
                            .findById(
                                    dto.getCountryId())
                            .orElseThrow();
        }

        Holiday holiday =
                Holiday.builder()
                        .holidayName(dto.getHolidayName())
                        .holidayDate(dto.getHolidayDate())
                        .holidayType(dto.getHolidayType())
                        .isGlobal(dto.getIsGlobal())
                        .description(dto.getDescription())
                        .country(country)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        holiday =
                holidayRepository.save(
                        holiday);

        return convertToDto(
                holiday);
    }

    @Override
    public HolidayDto update(
            Long id,
            HolidayDto dto) {

        Holiday holiday =
                holidayRepository
                        .findById(id)
                        .orElseThrow();

        Country country = null;

        if (dto.getCountryId() != null) {

            country =
                    countryRepository
                            .findById(
                                    dto.getCountryId())
                            .orElseThrow();
        }

        holiday.setHolidayName(
                dto.getHolidayName());

        holiday.setHolidayDate(
                dto.getHolidayDate());

        holiday.setHolidayType(
                dto.getHolidayType());

        holiday.setDescription(
                dto.getDescription());

        holiday.setIsGlobal(
                dto.getIsGlobal());

        holiday.setCountry(
                country);

        holiday.setUpdatedAt(
                LocalDateTime.now());

        holiday =
                holidayRepository.save(
                        holiday);

        return convertToDto(
                holiday);
    }

    @Override
    public void delete(
            Long id) {

        holidayRepository.deleteById(
                id);
    }

    private HolidayDto convertToDto(
            Holiday holiday) {

        return HolidayDto.builder()
                .id(holiday.getId())
                .holidayName(
                        holiday.getHolidayName())
                .holidayDate(
                        holiday.getHolidayDate())
                .holidayType(
                        holiday.getHolidayType())
                .isGlobal(
                        holiday.getIsGlobal())
                .countryId(
                        holiday.getCountry() != null
                                ? holiday.getCountry().getId()
                                : null)
                .countryName(
                        holiday.getCountry() != null
                                ? holiday.getCountry().getCountryName()
                                : null)
                .description(
                        holiday.getDescription())
                .build();
    }
    
    @Override
    public List<HolidayDto> getEmployeeHolidays(
            Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        List<Long> countryIds =
                userCountryMappingRepository
                        .findByUser(user)
                        .stream()
                        .map(mapping ->
                                mapping.getCountry()
                                       .getId())
                        .toList();

        return holidayRepository
                .findUpcomingHolidays(
                        LocalDate.now())
                .stream()
                .filter(holiday ->

                        Boolean.TRUE.equals(
                                holiday.getIsGlobal())

                        ||

                        (holiday.getCountry() != null

                         &&

                         countryIds.contains(
                                 holiday.getCountry()
                                        .getId()))
                )
                .map(this::convertToDto)
                .limit(10)
                .toList();
    }
    
    
}
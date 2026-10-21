package com.boit.boit_leave.service.impl;

import com.boit.boit_leave.dto.LeaveCalculationResponseDto;
import com.boit.boit_leave.entity.Holiday;
import com.boit.boit_leave.entity.LeaveDurationType;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.HolidayRepository;
import com.boit.boit_leave.repository.UserCountryMappingRepository;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.service.LeaveCalculationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveCalculationServiceImpl
        implements LeaveCalculationService {

    private final HolidayRepository holidayRepository;

    private final UserRepository userRepository;

    private final UserCountryMappingRepository
            userCountryMappingRepository;

    @Override
    public BigDecimal calculateLeaveDays(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveDurationType duration) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        /*
         * Half-day leave must be for a single date.
         */
        if (duration != LeaveDurationType.FULL_DAY
                && !startDate.equals(endDate)) {

            throw new IllegalArgumentException(
                    "Half-day leave must be for a single date"
            );
        }

        List<Long> countryIds =
                userCountryMappingRepository
                        .findByUser(user)
                        .stream()
                        .map(mapping ->
                                mapping.getCountry().getId())
                        .toList();

        List<Holiday> holidays =
                holidayRepository
                        .findByHolidayDateBetween(
                                startDate,
                                endDate
                        );

        Set<LocalDate> holidayDates =
                holidays.stream()
                        .filter(h ->
                                Boolean.TRUE.equals(
                                        h.getIsGlobal())
                                ||
                                (
                                    h.getCountry() != null
                                    &&
                                    countryIds.contains(
                                            h.getCountry().getId()
                                    )
                                )
                        )
                        .map(Holiday::getHolidayDate)
                        .collect(Collectors.toSet());

        /*
         * Half-day calculation
         */
        if (duration == LeaveDurationType.FIRST_HALF
                || duration == LeaveDurationType.SECOND_HALF) {

            LocalDate date = startDate;

            boolean weekend =
                    date.getDayOfWeek() == DayOfWeek.SATURDAY
                    ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY;

            boolean holiday =
                    holidayDates.contains(date);

            if (weekend || holiday) {
                return BigDecimal.ZERO;
            }

            return new BigDecimal("0.5");
        }

        /*
         * Existing full-day calculation.
         */
        long leaveDays = 0;

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {

            boolean weekend =
                    current.getDayOfWeek()
                            == DayOfWeek.SATURDAY
                    ||
                    current.getDayOfWeek()
                            == DayOfWeek.SUNDAY;

            boolean holiday =
                    holidayDates.contains(current);

            if (!weekend && !holiday) {
                leaveDays++;
            }

            current = current.plusDays(1);
        }

        return BigDecimal.valueOf(leaveDays);
    }

    @Override
    public LeaveCalculationResponseDto
    getLeaveCalculationDetails(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveDurationType duration) {

        List<String> excludedDays =
                new ArrayList<>();

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        /*
         * Half-day validation
         */
        if (duration != LeaveDurationType.FULL_DAY
                && !startDate.equals(endDate)) {

            throw new IllegalArgumentException(
                    "Half-day leave must be for a single date"
            );
        }

        List<Long> countryIds =
                userCountryMappingRepository
                        .findByUser(user)
                        .stream()
                        .map(mapping ->
                                mapping.getCountry().getId())
                        .toList();

        List<Holiday> holidays =
                holidayRepository
                        .findByHolidayDateBetween(
                                startDate,
                                endDate
                        );

        /*
         * Half-day calculation details
         */
        if (duration == LeaveDurationType.FIRST_HALF
                || duration == LeaveDurationType.SECOND_HALF) {

            LocalDate date = startDate;

            boolean weekend =
                    date.getDayOfWeek() == DayOfWeek.SATURDAY
                    ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY;

            Holiday holidayMatch =
                    holidays.stream()
                            .filter(h ->
                                    h.getHolidayDate()
                                            .equals(date))
                            .filter(h ->
                                    Boolean.TRUE.equals(
                                            h.getIsGlobal())
                                    ||
                                    (
                                        h.getCountry() != null
                                        &&
                                        countryIds.contains(
                                                h.getCountry().getId()
                                        )
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (weekend) {

                excludedDays.add(
                        date + " (Weekend)"
                );

                return LeaveCalculationResponseDto
                        .builder()
                        .totalLeaveDays(BigDecimal.ZERO)
                        .excludedDays(excludedDays)
                        .build();
            }

            if (holidayMatch != null) {

                excludedDays.add(
                        date
                                + " ("
                                + holidayMatch.getHolidayName()
                                + ")"
                );

                return LeaveCalculationResponseDto
                        .builder()
                        .totalLeaveDays(BigDecimal.ZERO)
                        .excludedDays(excludedDays)
                        .build();
            }

            return LeaveCalculationResponseDto
                    .builder()
                    .totalLeaveDays(new BigDecimal("0.5"))
                    .excludedDays(excludedDays)
                    .build();
        }

        /*
         * Existing full-day calculation.
         */
        long leaveDays = 0;

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {

            boolean weekend =
                    current.getDayOfWeek()
                            == DayOfWeek.SATURDAY
                    ||
                    current.getDayOfWeek()
                            == DayOfWeek.SUNDAY;

            LocalDate checkDate = current;

            Holiday holidayMatch =
                    holidays.stream()
                            .filter(h ->
                                    h.getHolidayDate()
                                            .equals(checkDate))
                            .filter(h ->
                                    Boolean.TRUE.equals(
                                            h.getIsGlobal())
                                    ||
                                    (
                                        h.getCountry() != null
                                        &&
                                        countryIds.contains(
                                                h.getCountry().getId()
                                        )
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (weekend) {

                excludedDays.add(
                        current + " (Weekend)"
                );

            } else if (holidayMatch != null) {

                excludedDays.add(
                        current
                                + " ("
                                + holidayMatch.getHolidayName()
                                + ")"
                );

            } else {

                leaveDays++;
            }

            current = current.plusDays(1);
        }

        return LeaveCalculationResponseDto
                .builder()
                .totalLeaveDays(
                        BigDecimal.valueOf(leaveDays)
                )
                .excludedDays(excludedDays)
                .build();
    }
}
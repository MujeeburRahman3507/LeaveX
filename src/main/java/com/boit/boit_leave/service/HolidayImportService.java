package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.HolidayCsvRowDto;
import com.boit.boit_leave.dto.HolidayImportPreviewDto;
import com.boit.boit_leave.dto.HolidayImportRequest;
import com.boit.boit_leave.dto.HolidayImportRowResult;
import com.boit.boit_leave.entity.Country;
import com.boit.boit_leave.entity.Holiday;
import com.boit.boit_leave.repository.CountryRepository;
import com.boit.boit_leave.repository.HolidayRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HolidayImportService {

    private final HolidayCsvParserService holidayCsvParserService;

    private final HolidayRepository holidayRepository;

    private final CountryRepository countryRepository;

    public HolidayImportPreviewDto preview(
            HolidayImportRequest request,
            MultipartFile file
    ) throws IOException {

        validateRequest(request);

        Country country =
                countryRepository
                        .findById(request.getCountryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Country not found."
                                )
                        );

        List<HolidayCsvRowDto> csvRows =
                holidayCsvParserService.parse(file);

        LocalDate yearStart =
                LocalDate.of(
                        request.getYear(),
                        1,
                        1
                );

        LocalDate yearEnd =
                LocalDate.of(
                        request.getYear(),
                        12,
                        31
                );

        List<Holiday> existingHolidays =
                holidayRepository
                        .findByCountryIdAndHolidayDateBetween(
                                request.getCountryId(),
                                yearStart,
                                yearEnd
                        );

        Set<String> existingHolidayKeys =
                new HashSet<>();

        for (Holiday holiday :
                existingHolidays) {

            String key =
                    buildDuplicateKey(
                            holiday.getHolidayDate(),
                            holiday.getHolidayName()
                    );

            existingHolidayKeys.add(key);
        }

        List<HolidayImportRowResult> results =
                new java.util.ArrayList<>();

        int validRows = 0;
        int duplicateRows = 0;
        int errorRows = 0;

        Set<String> csvKeys =
                new HashSet<>();

        int rowNumber = 1;

        for (HolidayCsvRowDto row : csvRows) {

            rowNumber++;

            String errorMessage =
                    validateRow(
                            row,
                            request.getYear()
                    );

            if (errorMessage != null) {

                errorRows++;

                results.add(
                        HolidayImportRowResult.builder()
                                .rowNumber(rowNumber)
                                .holidayName(
                                        row.getHolidayName()
                                )
                                .holidayDate(
                                        row.getHolidayDate()
                                )
                                .holidayType(
                                        row.getHolidayType()
                                )
                                .status("ERROR")
                                .message(errorMessage)
                                .build()
                );

                continue;
            }

            LocalDate holidayDate =
                    LocalDate.parse(
                            row.getHolidayDate()
                    );

            String duplicateKey =
                    buildDuplicateKey(
                            holidayDate,
                            row.getHolidayName()
                    );

            if (
                    existingHolidayKeys.contains(
                            duplicateKey
                    )
                    ||
                    csvKeys.contains(
                            duplicateKey
                    )
            ) {

                duplicateRows++;

                results.add(
                        HolidayImportRowResult.builder()
                                .rowNumber(rowNumber)
                                .holidayName(
                                        row.getHolidayName()
                                )
                                .holidayDate(
                                        row.getHolidayDate()
                                )
                                .holidayType(
                                        row.getHolidayType()
                                )
                                .status("DUPLICATE")
                                .message(
                                        "Holiday already exists."
                                )
                                .build()
                );

                continue;
            }

            csvKeys.add(duplicateKey);

            validRows++;

            results.add(
                    HolidayImportRowResult.builder()
                            .rowNumber(rowNumber)
                            .holidayName(
                                    row.getHolidayName()
                            )
                            .holidayDate(
                                    row.getHolidayDate()
                            )
                            .holidayType(
                                    row.getHolidayType()
                            )
                            .status("VALID")
                            .message(
                                    "Ready for import."
                            )
                            .build()
            );
        }

        return HolidayImportPreviewDto.builder()

                .countryId(
                        country.getId()
                )

                .countryName(
                        country.getCountryName()
                )

                .year(
                        request.getYear()
                )

                .importMode(
                        request.getImportMode()
                                .name()
                )

                .totalRows(
                        csvRows.size()
                )

                .validRows(
                        validRows
                )

                .duplicateRows(
                        duplicateRows
                )

                .errorRows(
                        errorRows
                )

                .rows(
                        results
                )

                .build();
    }

    private String validateRow(
            HolidayCsvRowDto row,
            Integer year
    ) {

        if (
                row.getHolidayName() == null
                ||
                row.getHolidayName()
                        .isBlank()
        ) {

            return "Holiday name is required.";
        }

        if (
                row.getHolidayDate() == null
                ||
                row.getHolidayDate()
                        .isBlank()
        ) {

            return "Holiday date is required.";
        }

        if (
                row.getHolidayType() == null
                ||
                row.getHolidayType()
                        .isBlank()
        ) {

            return "Holiday type is required.";
        }

        LocalDate date;

        try {

            date =
                    LocalDate.parse(
                            row.getHolidayDate()
                    );

        } catch (DateTimeException ex) {

            return
                    "Invalid date. Expected format: yyyy-MM-dd.";
        }

        if (
                date.getYear()
                !=
                year
        ) {

            return
                    "Holiday date does not belong to selected year.";
        }

        String holidayType =
                row.getHolidayType()
                        .trim()
                        .toUpperCase();

        if (
                !holidayType.equals("PUBLIC")
                &&
                !holidayType.equals("BANK")
        ) {

            return
                    "Holiday type must be PUBLIC or BANK.";
        }

        return null;
    }

    private String buildDuplicateKey(
            LocalDate date,
            String holidayName
    ) {

        return date
                + "|"
                + holidayName
                        .trim()
                        .toLowerCase();
    }

    private void validateRequest(
            HolidayImportRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Import request is required."
            );
        }

        if (request.getCountryId() == null) {

            throw new IllegalArgumentException(
                    "Country is required."
            );
        }

        if (request.getYear() == null) {

            throw new IllegalArgumentException(
                    "Year is required."
            );
        }

        if (
                request.getYear() < 2000
                ||
                request.getYear() > 2100
        ) {

            throw new IllegalArgumentException(
                    "Invalid holiday year."
            );
        }

        if (request.getImportMode() == null) {

            throw new IllegalArgumentException(
                    "Import mode is required."
            );
        }
    }
    
    @Transactional
    public HolidayImportPreviewDto importHolidays(
            HolidayImportRequest request,
            MultipartFile file
    ) throws IOException {

        validateRequest(request);

        Country country =
                countryRepository
                        .findById(request.getCountryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Country not found."
                                )
                        );

        // Validate the CSV first.
        HolidayImportPreviewDto preview =
                preview(request, file);

        if (preview.getErrorRows() > 0) {
            throw new IllegalArgumentException(
                    "CSV contains invalid rows. Please fix the errors before importing."
            );
        }

        LocalDate yearStart =
                LocalDate.of(
                        request.getYear(),
                        1,
                        1
                );

        LocalDate yearEnd =
                LocalDate.of(
                        request.getYear(),
                        12,
                        31
                );

        /*
         * REPLACE mode:
         * Delete only holidays belonging to the selected
         * country and selected year.
         */
        if (request.getImportMode() ==
                com.boit.boit_leave.entity.HolidayImportMode.REPLACE) {

            holidayRepository
                    .deleteByCountryIdAndHolidayDateBetween(
                            request.getCountryId(),
                            yearStart,
                            yearEnd
                    );
        }

        List<HolidayCsvRowDto> csvRows =
                holidayCsvParserService.parse(file);

        int importedRows = 0;
        int skippedRows = 0;

        for (HolidayCsvRowDto row : csvRows) {

            LocalDate holidayDate =
                    LocalDate.parse(
                            row.getHolidayDate()
                    );

            /*
             * In ADD mode, skip existing duplicates.
             *
             * In REPLACE mode, old records were already
             * deleted, so the CSV can be inserted directly.
             */
            if (
                    request.getImportMode() ==
                            com.boit.boit_leave.entity.HolidayImportMode.ADD
            ) {

                List<Holiday> existing =
                        holidayRepository
                                .findByCountryIdAndHolidayDateBetween(
                                        request.getCountryId(),
                                        holidayDate,
                                        holidayDate
                                );

                boolean duplicate =
                        existing.stream()
                                .anyMatch(existingHoliday ->
                                        existingHoliday
                                                .getHolidayName()
                                                .trim()
                                                .equalsIgnoreCase(
                                                        row.getHolidayName()
                                                                .trim()
                                                )
                                );

                if (duplicate) {
                    skippedRows++;
                    continue;
                }
            }

            Holiday holiday =
                    new Holiday();

            holiday.setHolidayName(
                    row.getHolidayName().trim()
            );

            holiday.setHolidayDate(
                    holidayDate
            );

            holiday.setHolidayType(
                    row.getHolidayType()
                            .trim()
                            .toUpperCase()
            );

            holiday.setDescription(
                    row.getDescription()
            );

            holiday.setIsGlobal(false);

            holiday.setCountry(
                    country
            );

            holidayRepository.save(
                    holiday
            );

            importedRows++;
        }

        /*
         * Return a fresh preview after the import.
         * This gives the frontend the final state.
         */
        HolidayImportPreviewDto result =
                preview(request, file);

        result.setValidRows(importedRows);
        result.setDuplicateRows(skippedRows);

        return result;
    }
}
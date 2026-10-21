package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.HolidayDto;
import com.boit.boit_leave.service.HolidayImportService;
import com.boit.boit_leave.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.boit.boit_leave.dto.HolidayImportPreviewDto;
import com.boit.boit_leave.dto.HolidayImportRequest;
import com.boit.boit_leave.entity.HolidayImportMode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/holidays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HolidayController {

	private final HolidayService holidayService;

	private final HolidayImportService holidayImportService;

    @GetMapping
    public List<HolidayDto> getAll() {

        return holidayService.getAll();
    }

    @PostMapping
    public HolidayDto create(
            @RequestBody HolidayDto dto) {

        return holidayService.create(
                dto);
    }

    @PutMapping("/{id}")
    public HolidayDto update(
            @PathVariable Long id,
            @RequestBody HolidayDto dto) {

        return holidayService.update(
                id,
                dto);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        holidayService.delete(id);
    }
    
    @PostMapping(
            value = "/import/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public HolidayImportPreviewDto previewHolidayImport(
            @RequestParam("countryId") Long countryId,
            @RequestParam("year") Integer year,
            @RequestParam("importMode") HolidayImportMode importMode,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        HolidayImportRequest request =
                HolidayImportRequest.builder()
                        .countryId(countryId)
                        .year(year)
                        .importMode(importMode)
                        .build();

        return holidayImportService.preview(
                request,
                file
        );
    }
    
    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public HolidayImportPreviewDto importHolidays(
            @RequestParam("countryId") Long countryId,
            @RequestParam("year") Integer year,
            @RequestParam("importMode") HolidayImportMode importMode,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        HolidayImportRequest request =
                HolidayImportRequest.builder()
                        .countryId(countryId)
                        .year(year)
                        .importMode(importMode)
                        .build();

        return holidayImportService.importHolidays(
                request,
                file
        );
    }
}
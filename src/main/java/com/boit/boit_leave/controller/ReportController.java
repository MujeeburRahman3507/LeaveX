
package com.boit.boit_leave.controller;

import com.boit.boit_leave.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ExportService exportService;

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String leaveType,

            @RequestParam(required = false)
            String status) {

        byte[] report = exportService.generateExcelReport(
                startDate, endDate, leaveType, status
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("boit-leave-report.xlsx")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }

    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String leaveType,

            @RequestParam(required = false)
            String status) {

        byte[] report = exportService.generatePdfReport(
                startDate, endDate, leaveType, status
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("boit-leave-report.pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }
}

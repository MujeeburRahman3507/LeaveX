package com.boit.boit_leave.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.boit.boit_leave.dto.ReportRowDto;
import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExportService {

    private final LeaveRequestRepository leaveRequestRepository;

    public byte[] generateExcelReport(
            LocalDate startDate,
            LocalDate endDate,
            String leaveType,
            String status) {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            XSSFSheet sheet =
                    workbook.createSheet(
                            "BOIT Leave Report"
                    );

            createReportHeader(
                    workbook,
                    sheet
            );

            createColumnHeaders(
                    workbook,
                    sheet
            );

            List<ReportRowDto> reportRows =
                    buildReportRows(startDate, endDate, leaveType, status);

            populateRows(
                    workbook,
                    sheet,
                    reportRows
            );

            autoSizeColumns(sheet);

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException ex) {

            log.error(
                    "Failed to generate excel report",
                    ex
            );

            throw new RuntimeException(
                    "Unable to generate Excel report",
                    ex
            );
        }
    }

    private void createReportHeader(
            XSSFWorkbook workbook,
            XSSFSheet sheet) {

        Row titleRow =
                sheet.createRow(0);

        Cell titleCell =
                titleRow.createCell(0);

        titleCell.setCellValue(
                "BOIT GLOBAL LEAVE MANAGEMENT REPORT"
        );

        CellStyle titleStyle =
                workbook.createCellStyle();

        Font titleFont =
                workbook.createFont();

        titleFont.setBold(true);
        titleFont.setFontHeightInPoints(
                (short) 16
        );

        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        8
                )
        );
    }

    private void createColumnHeaders(
            XSSFWorkbook workbook,
            XSSFSheet sheet) {

        Row headerRow =
                sheet.createRow(2);

        String[] columns = {
                "Request ID",
                "Employee Code",
                "Employee Name",
                "Leave Type",
                "Start Date",
                "End Date",
                "Total Days",
                "Status",
                "Approver"
        };

        CellStyle headerStyle =
                workbook.createCellStyle();

        Font headerFont =
                workbook.createFont();

        headerFont.setBold(true);

        headerStyle.setFont(headerFont);

        headerStyle.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        headerStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        headerStyle.setBorderBottom(
                BorderStyle.THIN
        );

        headerStyle.setBorderTop(
                BorderStyle.THIN
        );

        headerStyle.setBorderLeft(
                BorderStyle.THIN
        );

        headerStyle.setBorderRight(
                BorderStyle.THIN
        );

        headerStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        for (int i = 0; i < columns.length; i++) {

            Cell cell =
                    headerRow.createCell(i);

            cell.setCellValue(
                    columns[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }
    }

    private void populateRows(
            XSSFWorkbook workbook,
            XSSFSheet sheet,
            List<ReportRowDto> reportRows) {

        CellStyle dataStyle =
                workbook.createCellStyle();

        dataStyle.setBorderBottom(
                BorderStyle.THIN
        );

        dataStyle.setBorderTop(
                BorderStyle.THIN
        );

        dataStyle.setBorderLeft(
                BorderStyle.THIN
        );

        dataStyle.setBorderRight(
                BorderStyle.THIN
        );

        dataStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy"
                );

        int rowNum = 3;

        for (ReportRowDto rowData : reportRows) {

            Row row =
                    sheet.createRow(rowNum++);

            int col = 0;

            createCell(
                    row,
                    col++,
                    rowData.getRequestId(),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getEmployeeCode(),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getEmployeeName(),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getLeaveType(),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getStartDate()
                            .format(formatter),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getEndDate()
                            .format(formatter),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getTotalDays(),
                    dataStyle
            );

            createCell(
                    row,
                    col++,
                    rowData.getStatus(),
                    dataStyle
            );

            createCell(
                    row,
                    col,
                    rowData.getApprover(),
                    dataStyle
            );
        }
    }

    private void createCell(
            Row row,
            int column,
            Object value,
            CellStyle style) {

        Cell cell =
                row.createCell(column);

        if (value instanceof Long) {

            cell.setCellValue(
                    (Long) value
            );

        } else if (value instanceof Integer) {

            cell.setCellValue(
                    (Integer) value
            );

        } else if (value instanceof BigDecimal) {

            cell.setCellValue(
                    ((BigDecimal) value).doubleValue()
            );

        } else {

            cell.setCellValue(
                    value != null
                            ? value.toString()
                            : ""
            );
        }

        cell.setCellStyle(style);
    }

    private void autoSizeColumns(
            XSSFSheet sheet) {

        for (int i = 0; i < 9; i++) {

            sheet.autoSizeColumn(i);

            sheet.setColumnWidth(
                    i,
                    sheet.getColumnWidth(i) + 1000
            );
        }
    }

    private List<ReportRowDto> buildReportRows(
            LocalDate startDate,
            LocalDate endDate,
            String leaveType,
            String status) {

        if (startDate != null && endDate != null
                && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be on or before end date"
            );
        }

        return leaveRequestRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(request ->
                        startDate == null
                                || !request.getEndDate().isBefore(startDate))
                .filter(request ->
                        endDate == null
                                || !request.getStartDate().isAfter(endDate))
                .filter(request ->
                        leaveType == null
                                || leaveType.isBlank()
                                || request.getLeaveType().name()
                                        .equalsIgnoreCase(leaveType.trim()))
                .filter(request ->
                        status == null
                                || status.isBlank()
                                || request.getStatus().name()
                                        .equalsIgnoreCase(status.trim()))
                .map(this::mapToReportRow)
                .collect(Collectors.toList());
    }
    
    private ReportRowDto mapToReportRow(
            LeaveRequest request) {

        return ReportRowDto.builder()
                .requestId(
                        request.getId()
                )
                .employeeCode(
                        request.getUser()
                                .getEmployeeCode()
                )
                .employeeName(
                        request.getUser()
                                .getFullName()
                )
                .leaveType(
                	    request.getLeaveType().name()
                	)
                .startDate(
                        request.getStartDate()
                )
                .endDate(
                        request.getEndDate()
                )
                .totalDays(
                        request.getTotalDays()
                )
                .status(
                        request.getStatus().name()
                )
                .approver(
                        request.getApprover() != null
                                ? request.getApprover()
                                .getFullName()
                                : "-"
                )
                .build();
    }


    public byte[] generatePdfReport(
            LocalDate startDate,
            LocalDate endDate,
            String leaveType,
            String status) {

    try {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4.rotate(),
                        20,
                        20,
                        20,
                        20
                );

        PdfWriter.getInstance(
                document,
                outputStream
        );

        document.open();

        addPdfTitle(document);

        PdfPTable table = createPdfTable();

        List<ReportRowDto> reportRows =
                buildReportRows(startDate, endDate, leaveType, status);

        populatePdfRows(table, reportRows);

        document.add(table);

        document.close();

        return outputStream.toByteArray();

    } catch (Exception ex) {

        log.error(
                "Failed to generate PDF report",
                ex
        );

        throw new RuntimeException(
                "Unable to generate PDF report",
                ex
        );
    }
}

private void addPdfTitle(
        Document document)
        throws Exception {

    com.lowagie.text.Font titleFont =
            new com.lowagie.text.Font(
                    com.lowagie.text.Font.HELVETICA,
                    16,
                    com.lowagie.text.Font.BOLD
            );

    Paragraph title =
            new Paragraph(
                    "BOIT GLOBAL LEAVE MANAGEMENT REPORT",
                    titleFont
            );

    title.setAlignment(
            Element.ALIGN_CENTER
    );

    title.setSpacingAfter(20);

    document.add(title);
}

private PdfPTable createPdfTable() {

    PdfPTable table =
            new PdfPTable(9);

    table.setWidthPercentage(100);

    table.setSpacingBefore(10);

    table.setWidths(
            new float[]{
                    2f,
                    3f,
                    4f,
                    3f,
                    3f,
                    3f,
                    2f,
                    3f,
                    4f
            }
    );

    addPdfHeaderCell(
            table,
            "Request ID"
    );

    addPdfHeaderCell(
            table,
            "Employee Code"
    );

    addPdfHeaderCell(
            table,
            "Employee Name"
    );

    addPdfHeaderCell(
            table,
            "Leave Type"
    );

    addPdfHeaderCell(
            table,
            "Start Date"
    );

    addPdfHeaderCell(
            table,
            "End Date"
    );

    addPdfHeaderCell(
            table,
            "Days"
    );

    addPdfHeaderCell(
            table,
            "Status"
    );

    addPdfHeaderCell(
            table,
            "Approver"
    );

    return table;
}

private void addPdfHeaderCell(
        PdfPTable table,
        String value) {

	com.lowagie.text.Font font =
	        new com.lowagie.text.Font(
	                com.lowagie.text.Font.HELVETICA,
	                10,
	                com.lowagie.text.Font.BOLD
	        );

    PdfPCell cell =
            new PdfPCell(
                    new Paragraph(
                            value,
                            font
                    )
            );

    cell.setPadding(8);

    cell.setHorizontalAlignment(
            Element.ALIGN_CENTER
    );

    cell.setVerticalAlignment(
            Element.ALIGN_MIDDLE
    );

    cell.setBorder(
            Rectangle.BOX
    );

    table.addCell(cell);
}

private void populatePdfRows(
        PdfPTable table,
        List<ReportRowDto> reportRows) {

//    List<ReportRowDto> reportRows =
//            buildReportRows();

    for (ReportRowDto row : reportRows) {

        addPdfCell(
                table,
                String.valueOf(
                        row.getRequestId())
        );

        addPdfCell(
                table,
                row.getEmployeeCode()
        );

        addPdfCell(
                table,
                row.getEmployeeName()
        );

        addPdfCell(
                table,
                row.getLeaveType()
        );

        addPdfCell(
                table,
                String.valueOf(
                        row.getStartDate())
        );

        addPdfCell(
                table,
                String.valueOf(
                        row.getEndDate())
        );

        addPdfCell(
                table,
                String.valueOf(
                        row.getTotalDays())
        );

        addPdfCell(
                table,
                row.getStatus()
        );

        addPdfCell(
                table,
                row.getApprover()
        );
    }
}

private void addPdfCell(
        PdfPTable table,
        String value) {

	com.lowagie.text.Font font =
	        new com.lowagie.text.Font(
	                com.lowagie.text.Font.HELVETICA,
	                9,
	                com.lowagie.text.Font.NORMAL
	        );

    PdfPCell cell =
            new PdfPCell(
                    new Paragraph(
                            value,
                            font
                    )
            );

    cell.setPadding(6);

    cell.setBorder(
            Rectangle.BOX
    );

    table.addCell(cell);
}
}
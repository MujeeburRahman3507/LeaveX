package com.boit.boit_leave.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayImportPreviewDto {

    private Long countryId;

    private String countryName;

    private Integer year;

    private String importMode;

    private Integer totalRows;

    private Integer validRows;

    private Integer duplicateRows;

    private Integer errorRows;

    private List<HolidayImportRowResult> rows;

}
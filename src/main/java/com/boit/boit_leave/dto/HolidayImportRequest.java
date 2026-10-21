package com.boit.boit_leave.dto;

import com.boit.boit_leave.entity.HolidayImportMode;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayImportRequest {

    private Long countryId;

    private Integer year;

    private HolidayImportMode importMode;

}
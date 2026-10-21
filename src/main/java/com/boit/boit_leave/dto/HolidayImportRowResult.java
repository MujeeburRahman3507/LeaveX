package com.boit.boit_leave.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayImportRowResult {

    private Integer rowNumber;

    private String holidayName;

    private String holidayDate;

    private String holidayType;

    private String status;

    private String message;

}
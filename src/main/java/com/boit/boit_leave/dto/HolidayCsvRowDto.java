package com.boit.boit_leave.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayCsvRowDto {

    private String holidayName;

    private String holidayDate;

    private String holidayType;

    private String description;

}
package com.boit.boit_leave.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaturalLeaveRequestDto {

    private String leaveType;

    private String startDate;

    private String endDate;

    private String reason;
}
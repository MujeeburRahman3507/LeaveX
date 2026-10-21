package com.boit.boit_leave.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackdatedLeaveConfigurationDto {

    private Long id;

    private Integer maxBackdatedDays;

    private Boolean medicalDocumentRequired;

    private Boolean active;
}
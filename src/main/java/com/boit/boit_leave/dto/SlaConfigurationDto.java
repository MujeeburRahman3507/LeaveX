package com.boit.boit_leave.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaConfigurationDto {

    private Long id;

    private Integer slaDuration;

    private String slaUnit;

    private String calculationType;

    private Integer warningBefore;

    private Boolean tokenExpirySameAsSla;

    private Boolean active;
}

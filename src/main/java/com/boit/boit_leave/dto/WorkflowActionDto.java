package com.boit.boit_leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowActionDto {

    @NotNull(message = "Request id is required")
    private Long requestId;

    @NotBlank(message = "Status is required")
    private String status;

    private String managerComments;
}

package com.boit.boit_leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsDto {

    private Long totalEmployees;

    private Long totalManagers;

    private Long totalAdmins;

    private Long totalRequests;

    private Long approvedRequests;

    private Long rejectedRequests;

    private Long pendingRequests;
}
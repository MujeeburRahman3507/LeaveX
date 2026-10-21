package com.boit.boit_leave.dto;

import java.time.LocalDateTime;

import com.boit.boit_leave.entity.DocumentPriority;
import com.boit.boit_leave.entity.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerActionDto {

    private Long requestId;

    private Long leaveRequestId;

    private String employeeName;

    private DocumentType documentType;

    private DocumentPriority priority;

    private LocalDateTime uploadedAt;

    private String uploadedBy;

}
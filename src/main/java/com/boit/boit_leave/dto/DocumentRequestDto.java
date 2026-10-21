package com.boit.boit_leave.dto;

import com.boit.boit_leave.entity.DocumentPriority;
import com.boit.boit_leave.entity.DocumentRequestStatus;
import com.boit.boit_leave.entity.DocumentStatus;
import com.boit.boit_leave.entity.DocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDto {

    private Long id;

    private Long leaveRequestId;

    private Long requestedBy;

    private String requestedByName;

    private DocumentType documentType;

    private String comments;

    private DocumentRequestStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;
    
    private DocumentPriority priority;

    private DocumentStatus documentStatus;
    
    private String rejectionComments;

}
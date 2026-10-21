package com.boit.boit_leave.appraisal.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalDocumentResponse {

    private Long id;

    private Long appraisalId;

    private Long goalId;

    private Long uploadedBy;

    private String originalFileName;

    private String storedFileName;

    private String contentType;

    private Long fileSize;

    private String documentType;

    private String remarks;

    private LocalDateTime createdAt;
}
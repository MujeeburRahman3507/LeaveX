
package com.boit.boit_leave.AI.dto;

import com.boit.boit_leave.entity.AiAnalysisStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentAnalysisStatusResponse {

    private Long documentId;

    private AiAnalysisStatus status;

    private DocumentAnalysisResponse analysis;

    private String errorMessage;

    private LocalDateTime completedAt;
}

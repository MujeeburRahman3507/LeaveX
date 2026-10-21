package com.boit.boit_leave.appraisal.dto;

import com.boit.boit_leave.appraisal.enums.AppraisalCycleStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalCycleResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDate appraisalStartDate;

    private LocalDate appraisalEndDate;

    private LocalDate goalSettingStartDate;

    private LocalDate goalSettingEndDate;

    private LocalDate selfReviewStartDate;

    private LocalDate selfReviewEndDate;

    private LocalDate managerReviewStartDate;

    private LocalDate managerReviewEndDate;

    private LocalDate hrReviewStartDate;

    private LocalDate hrReviewEndDate;

    private AppraisalCycleStatus status;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
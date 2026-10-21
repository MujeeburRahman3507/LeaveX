package com.boit.boit_leave.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationTemplateResponse {

    private Long id;

    private String templateCode;

    private String templateName;

    private String channel;

    private String title;

    private String message;

    private String emailSubject;

    private String emailBody;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
package com.boit.boit_leave.dto;

import lombok.Data;

@Data
public class NotificationTemplateRequest {

    private String templateCode;

    private String templateName;

    private String channel;

    private String title;

    private String message;

    private String emailSubject;

    private String emailBody;

    private Boolean active;
}
package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.NotificationTemplate;
import com.boit.boit_leave.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.boit.boit_leave.dto.NotificationTemplateRequest;
import com.boit.boit_leave.dto.NotificationTemplateResponse;
import com.boit.boit_leave.entity.NotificationTemplate;

import java.util.List;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    /**
     * Get an active notification template by template code.
     */
    @Transactional(readOnly = true)
    public NotificationTemplate getActiveTemplate(String templateCode) {

        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Template code cannot be null or empty"
            );
        }

        return templateRepository
                .findByTemplateCodeAndActiveTrue(templateCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Active notification template not found: "
                                        + templateCode
                        )
                );
    }

    /**
     * Replace placeholders in the supplied text.
     *
     * Example:
     *
     * Hello {employeeName}
     *
     * becomes:
     *
     * Hello Mujeebur Rahman
     */
    public String replacePlaceholders(
            String text,
            Map<String, String> variables) {

        if (text == null || text.isEmpty()) {
            return text;
        }

        if (variables == null || variables.isEmpty()) {
            return text;
        }

        String result = text;

        for (Map.Entry<String, String> entry : variables.entrySet()) {

            String placeholder = "{" + entry.getKey() + "}";

            String value = entry.getValue() == null
                    ? ""
                    : entry.getValue();

            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Resolve the in-app title from a template.
     */
    @Transactional(readOnly = true)
    public String resolveTitle(
            String templateCode,
            Map<String, String> variables) {

        NotificationTemplate template =
                getActiveTemplate(templateCode);

        return replacePlaceholders(
                template.getTitle(),
                variables
        );
    }

    /**
     * Resolve the in-app message from a template.
     */
    @Transactional(readOnly = true)
    public String resolveMessage(
            String templateCode,
            Map<String, String> variables) {

        NotificationTemplate template =
                getActiveTemplate(templateCode);

        return replacePlaceholders(
                template.getMessage(),
                variables
        );
    }

    /**
     * Resolve the email subject from a template.
     */
    @Transactional(readOnly = true)
    public String resolveEmailSubject(
            String templateCode,
            Map<String, String> variables) {

        NotificationTemplate template =
                getActiveTemplate(templateCode);

        return replacePlaceholders(
                template.getEmailSubject(),
                variables
        );
    }

    /**
     * Resolve the email body from a template.
     */
    @Transactional(readOnly = true)
    public String resolveEmailBody(
            String templateCode,
            Map<String, String> variables) {

        NotificationTemplate template =
                getActiveTemplate(templateCode);

        return replacePlaceholders(
                template.getEmailBody(),
                variables
        );
    }
    
    public List<NotificationTemplateResponse> getAllTemplates() {

        return templateRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    public NotificationTemplateResponse getTemplateById(Long id) {

        NotificationTemplate template =
        		templateRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification template not found: " + id
                                )
                        );

        return toResponse(template);
    }
    
    public NotificationTemplateResponse createTemplate(
            NotificationTemplateRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Template request is required"
            );
        }

        if (request.getTemplateCode() == null ||
                request.getTemplateCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Template code is required"
            );
        }

        if (request.getTemplateName() == null ||
                request.getTemplateName().isBlank()) {

            throw new IllegalArgumentException(
                    "Template name is required"
            );
        }

        if (templateRepository
                .findByTemplateCode(request.getTemplateCode().trim())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Template code already exists"
            );
        }

        NotificationTemplate template =
                NotificationTemplate.builder()
                        .templateCode(
                                request.getTemplateCode().trim()
                        )
                        .templateName(
                                request.getTemplateName().trim()
                        )
                        .channel(
                                request.getChannel() == null ||
                                        request.getChannel().isBlank()
                                        ? "BOTH"
                                        : request.getChannel().trim()
                        )
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .emailSubject(request.getEmailSubject())
                        .emailBody(request.getEmailBody())
                        .active(
                                request.getActive() == null
                                        ? true
                                        : request.getActive()
                        )
                        .build();

        NotificationTemplate saved =
        		templateRepository.save(template);

        return toResponse(saved);
    }
    
    public NotificationTemplateResponse updateTemplate(
            Long id,
            NotificationTemplateRequest request
    ) {

        NotificationTemplate template =
        		templateRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification template not found: " + id
                                )
                        );

        if (request.getTemplateName() == null ||
                request.getTemplateName().isBlank()) {

            throw new IllegalArgumentException(
                    "Template name is required"
            );
        }

        if (request.getChannel() == null ||
                request.getChannel().isBlank()) {

            throw new IllegalArgumentException(
                    "Channel is required"
            );
        }

        template.setTemplateName(
                request.getTemplateName().trim()
        );

        template.setChannel(
                request.getChannel().trim()
        );

        template.setTitle(request.getTitle());

        template.setMessage(request.getMessage());

        template.setEmailSubject(
                request.getEmailSubject()
        );

        template.setEmailBody(
                request.getEmailBody()
        );

        if (request.getActive() != null) {
            template.setActive(
                    request.getActive()
            );
        }

        NotificationTemplate saved =
        		templateRepository.save(template);

        return toResponse(saved);
    }
    
    public NotificationTemplateResponse toggleActive(Long id) {

        NotificationTemplate template =
        		templateRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification template not found: " + id
                                )
                        );

        template.setActive(
                !Boolean.TRUE.equals(
                        template.getActive()
                )
        );

        NotificationTemplate saved =
                templateRepository.save(template);

        return toResponse(saved);
    }
    
    private NotificationTemplateResponse toResponse(
            NotificationTemplate template
    ) {

        return NotificationTemplateResponse.builder()
                .id(template.getId())
                .templateCode(template.getTemplateCode())
                .templateName(template.getTemplateName())
                .channel(template.getChannel())
                .title(template.getTitle())
                .message(template.getMessage())
                .emailSubject(template.getEmailSubject())
                .emailBody(template.getEmailBody())
                .active(template.getActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
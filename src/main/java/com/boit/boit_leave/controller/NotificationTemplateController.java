package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.NotificationTemplateRequest;
import com.boit.boit_leave.dto.NotificationTemplateResponse;
import com.boit.boit_leave.service.NotificationTemplateService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notification-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @GetMapping
    public ResponseEntity<List<NotificationTemplateResponse>> getAllTemplates() {

        return ResponseEntity.ok(
                notificationTemplateService.getAllTemplates()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateResponse> getTemplate(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificationTemplateService.getTemplateById(id)
        );
    }

    @PostMapping
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @RequestBody NotificationTemplateRequest request
    ) {

        return ResponseEntity.ok(
                notificationTemplateService.createTemplate(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @RequestBody NotificationTemplateRequest request
    ) {

        return ResponseEntity.ok(
                notificationTemplateService.updateTemplate(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<NotificationTemplateResponse> toggleActive(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificationTemplateService.toggleActive(id)
        );
    }
}
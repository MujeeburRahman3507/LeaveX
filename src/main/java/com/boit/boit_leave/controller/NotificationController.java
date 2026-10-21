package com.boit.boit_leave.controller;

import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.boit.boit_leave.dto.NotificationDto;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>>
    getNotifications(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(
                notificationService.getNotifications(
                        user.getUserId()
                )
        );
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(
                notificationService.unreadCount(
                        user.getUserId()
                )
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        notificationService
                .markAllAsRead(
                        user.getUserId()
                );

        return ResponseEntity.ok().build();
    }
}
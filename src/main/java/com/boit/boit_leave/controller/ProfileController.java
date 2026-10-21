package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.*;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ProfileDto>
    profile(
            @AuthenticationPrincipal
            CustomUserDetails user) {

        return ResponseEntity.ok(
                userService.getProfile(
                        user.getUserId()));
    }

    @PutMapping
    public ResponseEntity<ProfileDto>
    update(
            @AuthenticationPrincipal
            CustomUserDetails user,
            @RequestBody
            UpdateProfileDto request) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        user.getUserId(),
                        request));
    }
    
    @PostMapping("/upload-photo")
    public String uploadPhoto(
            @AuthenticationPrincipal
            CustomUserDetails user,
            @RequestParam("file")
            MultipartFile file) {

        return userService.uploadPhoto(
                user.getUserId(),
                file
        );
    }
}
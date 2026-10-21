package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.*;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>>
    login(
            @Valid
            @RequestBody
            LoginRequestDto request) {

        LoginResponseDto response =
                authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponseDto>builder()
                        .success(true)
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>>
    register(
            @Valid
            @RequestBody
            RegisterRequestDto request) {

        UserResponseDto response =
                authService.register(request);

        return ResponseEntity.ok(
                ApiResponse.<UserResponseDto>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(response)
                        .build()
        );
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody
            ForgotPasswordRequestDto request) {

        authService
                .forgotPassword(
                        request.getEmail()
                );

        return ResponseEntity.ok(
                "OTP sent successfully"
        );
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody
            VerifyOtpRequestDto request) {

        boolean valid =
                authService.verifyOtp(
                        request.getEmail(),
                        request.getOtp()
                );

        return ResponseEntity.ok(valid);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody
            ResetPasswordRequestDto request) {

        authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                "Password reset successful"
        );
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal
            CustomUserDetails user,
            @RequestBody
            ChangePasswordDto request) {

        authService.changePassword(
                user.getUserId(),
                request);

        return ResponseEntity.ok(
                "Password changed successfully");
    }
}
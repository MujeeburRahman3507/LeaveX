package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.AiPromptDto;
import com.boit.boit_leave.dto.LeaveConflictResponseDto;
import com.boit.boit_leave.dto.LeaveRequestDto;
import com.boit.boit_leave.dto.NaturalLeaveRequestDto;
import com.boit.boit_leave.security.CustomUserDetails;
import com.boit.boit_leave.service.AiLeaveService;
import com.boit.boit_leave.service.ConflictDetectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiLeaveService aiLeaveService;
    
    private final ConflictDetectionService conflictDetectionService;

    @PostMapping("/parse-leave")
    public NaturalLeaveRequestDto parseLeave(
            @RequestBody AiPromptDto dto) {

        return aiLeaveService.parseLeave(
                dto.getPrompt());
    }
    
    @PostMapping("/check-conflict")
    public LeaveConflictResponseDto checkConflict(
            @AuthenticationPrincipal
            CustomUserDetails user,

            @RequestBody LeaveRequestDto dto) {

        return conflictDetectionService
                .checkConflict(
                        user.getUserId(),
                        dto.getStartDate(),
                        dto.getEndDate()
                );
    }
}
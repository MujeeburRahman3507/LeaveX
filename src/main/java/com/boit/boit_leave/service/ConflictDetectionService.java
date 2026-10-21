package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.LeaveConflictResponseDto;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.boit.boit_leave.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ConflictDetectionService {

    private final LeaveRequestRepository leaveRequestRepository;

    private final UserRepository userRepository;

    public LeaveConflictResponseDto checkConflict(
            Long userId,
            LocalDate startDate,
            LocalDate endDate) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        long count =
                leaveRequestRepository
                        .countConflictingLeaves(
                                user.getDepartment(),
                                startDate,
                                endDate);

        return LeaveConflictResponseDto
                .builder()
                .conflict(count >= 3)
                .employeesOnLeave(count)
                .department(user.getDepartment())
                .recommendation(
                        count >= 3
                                ? "Consider another date."
                                : "No conflict detected."
                )
                .build();
    }
}
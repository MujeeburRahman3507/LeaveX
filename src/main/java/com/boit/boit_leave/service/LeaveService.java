package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.CalendarEventDto;
import com.boit.boit_leave.dto.LeaveRequestDto;
import com.boit.boit_leave.dto.LeaveResponseDto;
import com.boit.boit_leave.entity.LeaveBalance;
import com.boit.boit_leave.entity.LeaveDocumentRequest;
import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.LeaveStatus;
import com.boit.boit_leave.entity.LeaveType;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.exception.InsufficientBalanceException;
import com.boit.boit_leave.exception.InvalidWorkflowActionException;
import com.boit.boit_leave.exception.ResourceNotFoundException;
import com.boit.boit_leave.repository.HolidayRepository;
import com.boit.boit_leave.repository.LeaveBalanceRepository;
import com.boit.boit_leave.repository.LeaveDocumentRequestRepository;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.boit.boit_leave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import com.boit.boit_leave.entity.LeaveDurationType;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.boit.boit_leave.entity.SlaStatus;
import com.boit.boit_leave.entity.LeaveActionType;
import com.boit.boit_leave.service.LeaveActionTokenService;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;
import com.boit.boit_leave.repository.UserCountryMappingRepository;
import com.boit.boit_leave.dto.BackupPersonDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final LeaveCalculationService
    leaveCalculationService;
    private final HolidayRepository
    holidayRepository;
    private final LeaveDocumentRequestService leaveDocumentRequestService;
    private final LeaveDocumentRequestRepository leaveDocumentRequestRepository;
    private final SlaConfigurationService slaConfigurationService;
    private final LeaveActionTokenService leaveActionTokenService;
    private final BackdatedLeaveValidationService
    backdatedLeaveValidationService;
    private final UserCountryMappingRepository
    userCountryMappingRepository;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    public LeaveResponseDto applyLeave(
            LeaveRequestDto dto,
            Long userId) {

        User employee = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found : " + userId));
        
        User backupPerson = null;

        if (dto.getBackupPersonId() != null) {

            backupPerson = userRepository
                    .findById(dto.getBackupPersonId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Backup person not found"
                            ));

            if (!backupPerson.getActive()) {
                throw new InvalidWorkflowActionException(
                        "Selected backup person is inactive"
                );
            }

            if (backupPerson.getId().equals(userId)) {
                throw new InvalidWorkflowActionException(
                        "You cannot select yourself as backup person"
                );
            }

            Long backupPersonId = backupPerson.getId();

            boolean validBackupPerson =
                    userCountryMappingRepository
                            .findBackupPersons(userId)
                            .stream()
                            .anyMatch(user ->
                                    user.getId().equals(backupPersonId)
                            );

            if (!validBackupPerson) {
                throw new InvalidWorkflowActionException(
                        "Backup person must belong to the same country"
                );
            }
        }

//        if (employee.getManager() == null) {
//            throw new InvalidWorkflowActionException(
//                    "Employee does not have assigned manager");
//        }
        
        // 2 APPROVAL FLOW//
        
        if (employee.getApprovalLevels() == null
                || employee.getLevel1Approver() == null) {
            throw new InvalidWorkflowActionException(
                    "Level 1 approver is not configured for this employee"
            );
        }

        if (Integer.valueOf(2).equals(employee.getApprovalLevels())
                && employee.getLevel2Approver() == null) {
            throw new InvalidWorkflowActionException(
                    "Level 2 approver is not configured for this employee"
            );
        }
        
        backdatedLeaveValidationService.validateBackdatedLeave(
                dto.getLeaveType() != null
                        ? dto.getLeaveType().name()
                        : null,
                dto.getStartDate(),
                dto.getEndDate(),
                LocalDate.now()
        );

        validateDateRange(
                dto.getStartDate(),
                dto.getEndDate()
        );

        validateOverlappingLeave(
                userId,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getDurationType()
        );

//        int totalDays = calculateWorkingDays(
//                dto.getStartDate(),
//                dto.getEndDate()
//        );
        
        BigDecimal  totalDays = leaveCalculationService
                .calculateLeaveDays(
                        userId,
                        dto.getStartDate(),
                        dto.getEndDate(),
                        dto.getDurationType()
                );
        
        BigDecimal requestedDays = totalDays;

        if (totalDays.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidWorkflowActionException(
                    "Leave request contains zero working days"
            );
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave balance not found"));

        BigDecimal availableBalance =
                balance.getAnnualBalance()
                        .subtract(balance.getUsedLeave());

//        if (availableBalance < totalDays) {
//
//            throw new InsufficientBalanceException(
//                    "Insufficient leave balance. Available="
//                            + availableBalance
//                            + ", Requested="
//                            + totalDays
//            );
//        }
        
        if (availableBalance.compareTo(requestedDays) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient leave balance. Available="
                            + availableBalance
                            + ", Requested="
                            + requestedDays
            );
        }
        
        LocalDateTime submittedAt = LocalDateTime.now();

        LocalDateTime slaDueAt =
                calculateSlaDueAt(submittedAt);

//        LeaveRequest request =
//                LeaveRequest.builder()
//                        .user(employee)
//                        .leaveType(
//                        	    dto.getLeaveType()
//                        	)
//                        .startDate(dto.getStartDate())
//                        .endDate(dto.getEndDate())
//                        .totalDays(requestedDays)
//                        .durationType(dto.getDurationType())
//                        .reason(dto.getReason())
//                        .status(LeaveStatus.PENDING)
//                        .createdAt(LocalDateTime.now())
//                        .updatedAt(LocalDateTime.now())
//                        .build();
        LeaveRequest request =
                LeaveRequest.builder()
                        .user(employee)
                        .backupPerson(backupPerson)
                        .leaveType(dto.getLeaveType())
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .totalDays(requestedDays)
                        .durationType(dto.getDurationType())
                        .reason(dto.getReason())
                        .status(LeaveStatus.PENDING)
                        .createdAt(submittedAt)
                        .updatedAt(submittedAt)
                        .slaDueAt(slaDueAt)
                        .slaWarningSent(false)
                        .slaBreachEmailSent(false)
                        .slaStatus(SlaStatus.WITHIN_SLA)
                        .build();

        LeaveRequest savedRequest =
                leaveRequestRepository.save(request);
        
        notificationService.createNotification(
                userId,
                "Leave Submitted",
                "Your leave request has been submitted successfully."
        );

        System.out.println(
                "Employee Id = " + employee.getId()
        );

        System.out.println(
                "Manager Id = " + employee.getManager().getId()
        );
        notificationService.createNotification(
//                employee.getManager().getId(), //
        		employee.getLevel1Approver().getId(),
                "New Leave Request",
                employee.getFullName()
                        + " has submitted a leave request."
        );
        auditService.logAction(
                "LEAVE_APPLIED",
                employee,
                "LEAVE_REQUEST",
                savedRequest.getId(),
                "Leave applied from "
                        + dto.getStartDate()
                        + " to "
                        + dto.getEndDate()
        );

//        emailService.sendManagerNotification(
//                employee.getManager(),
//                employee,
//                savedRequest
//        );
        
//        User manager =
//                userRepository.findById(
//                        employee.getManager().getId()
//                ).orElseThrow();
        
        User manager = userRepository.findById(
                employee.getLevel1Approver().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Level 1 approver not found")
        );

        Map<LeaveActionType, String> actionTokens =
                leaveActionTokenService.createActionTokens(
                        savedRequest,
                        manager,
                        savedRequest.getSlaDueAt()
                );

  

        String approveUrl =
                frontendUrl + "/leave-action?token="
                        + actionTokens.get(LeaveActionType.APPROVE);

        String rejectUrl =
                frontendUrl + "/leave-action?token="
                        + actionTokens.get(LeaveActionType.REJECT);

        String requestDocumentUrl =
                frontendUrl + "/leave-action?token="
                        + actionTokens.get(LeaveActionType.REQUEST_DOCUMENT);

        Map<String, String> emailVariables = new HashMap<>();
        
        emailVariables.put(
                "requestId",
                savedRequest.getId().toString()
        );

        emailVariables.put(
                "managerName",
                manager.getFullName()
        );

        emailVariables.put(
                "employeeName",
                employee.getFullName()
        );

        emailVariables.put(
                "leaveType",
                savedRequest.getLeaveType().name()
        );

        emailVariables.put(
                "duration",
                getDurationDisplay(savedRequest)
        );

        emailVariables.put(
                "startDate",
                savedRequest.getStartDate().toString()
        );

        emailVariables.put(
                "endDate",
                savedRequest.getEndDate().toString()
        );

        emailVariables.put(
                "totalDays",
                savedRequest.getTotalDays().toString()
        );

        emailVariables.put(
                "reason",
                savedRequest.getReason() != null
                        ? savedRequest.getReason()
                        : ""
        );

        emailVariables.put(
                "approveUrl",
                approveUrl
        );

        emailVariables.put(
                "rejectUrl",
                rejectUrl
        );

        emailVariables.put(
                "requestDocumentUrl",
                requestDocumentUrl
        );

        emailService.sendTemplateEmail(
                manager.getEmail(),
                "LEAVE_MANAGER_ACTION",
                emailVariables
        );
//        emailService.sendManagerNotification(
//                manager,
//                employee,
//                savedRequest
//        );

        log.info(
                "Leave request created successfully. RequestId={}",
                savedRequest.getId()
        );

        return convertToResponse(savedRequest);
    }
    
    private void validateOverlappingLeave(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveDurationType requestedDuration) {

        List<LeaveRequest> overlappingRequests =
                leaveRequestRepository
                        .findByUserIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                userId,
                                List.of(
                                        LeaveStatus.PENDING,
                                        LeaveStatus.APPROVED
                                ),
                                endDate,
                                startDate
                        );

        for (LeaveRequest existingRequest : overlappingRequests) {

            LeaveDurationType existingDuration =
                    existingRequest.getDurationType();

            /*
             * Defensive handling for old/null records.
             * Treat null as FULL_DAY.
             */
            if (existingDuration == null) {
                existingDuration = LeaveDurationType.FULL_DAY;
            }

            /*
             * Full-day conflicts with everything
             * on the same date/range.
             */
            if (existingDuration == LeaveDurationType.FULL_DAY
                    || requestedDuration == LeaveDurationType.FULL_DAY) {

                throw new InvalidWorkflowActionException(
                        "Overlapping leave request exists"
                );
            }

            /*
             * Same half conflicts with itself.
             *
             * FIRST_HALF + SECOND_HALF is allowed.
             */
            if (existingDuration == requestedDuration) {

                throw new InvalidWorkflowActionException(
                        "Overlapping leave request exists for the selected half-day"
                );
            }
        }
    }
    
    private LocalDateTime calculateSlaDueAt(
            LocalDateTime submittedAt) {

        var configuration =
                slaConfigurationService.getConfiguration();

        int duration = configuration.getSlaDuration();

        String unit = configuration.getSlaUnit();

        String calculationType =
                configuration.getCalculationType();

        if ("HOURS".equalsIgnoreCase(unit)) {

            return submittedAt.plusHours(duration);
        }

        if ("CALENDAR_DAYS".equalsIgnoreCase(
                calculationType)) {

            return submittedAt.plusDays(duration);
        }

        /*
         * WORKING_DAYS
         *
         * Count Monday-Friday and exclude
         * configured holidays.
         */
        return addWorkingDays(
                submittedAt,
                duration
        );
    }
    
    private LocalDateTime addWorkingDays(
            LocalDateTime submittedAt,
            int workingDays) {

        LocalDateTime result = submittedAt;

        int daysAdded = 0;

        while (daysAdded < workingDays) {

            result = result.plusDays(1);

            LocalDate date = result.toLocalDate();

            DayOfWeek dayOfWeek =
                    date.getDayOfWeek();

            boolean weekend =
                    dayOfWeek == DayOfWeek.SATURDAY
                            || dayOfWeek == DayOfWeek.SUNDAY;

            boolean configuredHoliday =
                    holidayRepository.findAll()
                            .stream()
                            .anyMatch(existingHoliday ->
                                    existingHoliday
                                            .getHolidayDate()
                                            .equals(date)
                            );

            if (!weekend && !configuredHoliday) {
                daysAdded++;
            }
        }

        return result;
    }
    
    
    
    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {

            throw new InvalidWorkflowActionException(
                    "Start date and end date are required"
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new InvalidWorkflowActionException(
                    "End date cannot be before start date"
            );
        }
    }
    
//    private int calculateWorkingDays(
//            LocalDate startDate,
//            LocalDate endDate) {
//
//        int workingDays = 0;
//
//        LocalDate current = startDate;
//
//        while (!current.isAfter(endDate)) {
//
//            DayOfWeek day =
//                    current.getDayOfWeek();
//
//            if (day != DayOfWeek.SATURDAY
//                    && day != DayOfWeek.SUNDAY) {
//
//                workingDays++;
//            }
//
//            current = current.plusDays(1);
//        }
//
//        return workingDays;
//    }
    
//    private LeaveResponseDto convertToResponse(
//            LeaveRequest request) {
//
//        return LeaveResponseDto.builder()
//                .id(request.getId())
//
//                .employeeId(
//                        request.getUser().getId())
//
//                .employeeName(
//                        request.getUser().getFullName())
//
//                .employeeEmail(
//                        request.getUser().getEmail())
//
//                .leaveType(
//                        request.getLeaveType().name()
//                )
//
//                .startDate(
//                        request.getStartDate())
//
//                .endDate(
//                        request.getEndDate())
//
//                .totalDays(
//                        request.getTotalDays())
//
//                .reason(
//                        request.getReason())
//
//                .status(
//                        request.getStatus().name())
//
//                .approverId(
//                        request.getApprover() != null
//                                ? request.getApprover().getId()
//                                : null)
//
//                .approverName(
//                        request.getApprover() != null
//                                ? request.getApprover().getFullName()
//                                : null)
//
//                .managerComments(
//                        request.getManagerComments())
//
//                .actionDate(
//                        request.getActionDate())
//
//                .createdAt(
//                        request.getCreatedAt())
//
//                .build();
//    }
    
    private LeaveResponseDto convertToResponse(
            LeaveRequest request) {

        LeaveResponseDto dto = LeaveResponseDto.builder()
                .id(request.getId())
                .employeeId(request.getUser().getId())
                .employeeName(request.getUser().getFullName())
                .employeeEmail(request.getUser().getEmail())
                .leaveType(request.getLeaveType().name())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .reason(request.getReason())
                .status(request.getStatus().name())
                .approverId(
                        request.getApprover() != null
                                ? request.getApprover().getId()
                                : null)
                .approverName(
                        request.getApprover() != null
                                ? request.getApprover().getFullName()
                                : null)
                .managerComments(request.getManagerComments())
                .actionDate(request.getActionDate())
                .createdAt(request.getCreatedAt())
                .slaStatus(
                        request.getSlaStatus() != null
                                ? request.getSlaStatus().name()
                                : null
                )

                .slaDueAt(
                        request.getSlaDueAt()
                )
                
                .backupPersonId(
                		request.getBackupPerson() != null
                	        ? request.getBackupPerson().getId()
                	        : null
                	)
                	.backupPersonName(
                			request.getBackupPerson() != null
                	        ? request.getBackupPerson().getFullName()
                	        : null
                	)
                .build();

        LeaveDocumentRequest latestRequest =
                leaveDocumentRequestRepository
                        .findFirstByLeaveRequestIdOrderByRequestedAtDesc(
                                request.getId()
                        )
                        .orElse(null);

        if (latestRequest != null) {

            dto.setDocumentRequested(true);

            dto.setDocumentStatus(
                    latestRequest.getDocumentStatus()
            );

            dto.setDocumentPriority(
                    latestRequest.getPriority()
            );

        } else {

            dto.setDocumentRequested(false);

        }

        return dto;
    }
//    
//    @Transactional(readOnly = true)
//    public List<LeaveRequest> getMyRequests(
//            Long userId) {
//
//        return leaveRequestRepository
//                .findByUserIdOrderByCreatedAtDesc(
//                        userId
//                );
//    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getMyRequests(
            Long userId) {

        return leaveRequestRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }
//    @Transactional(readOnly = true)
//    public List<LeaveRequest> getManagerPendingRequests(
//            Long managerId) {
//
//        return leaveRequestRepository
//                .findByUserManagerIdAndStatusOrderByCreatedAtDesc(
//                        managerId,
//                        LeaveStatus.PENDING
//                );
//    }
    
//    private LeaveResponseDto mapToResponseDto(
//            LeaveRequest request) {
//
//        return LeaveResponseDto.builder()
//                .id(request.getId())
//                .employeeId(request.getUser().getId())
//                .employeeName(request.getUser().getFullName())
//                .employeeEmail(request.getUser().getEmail())
//                .leaveType(request.getLeaveType().name())
//                .startDate(request.getStartDate())
//                .endDate(request.getEndDate())
//                .totalDays(request.getTotalDays())
//                .reason(request.getReason())
//                .status(request.getStatus().name())
//                .approverId(
//                        request.getApprover() != null
//                                ? request.getApprover().getId()
//                                : null)
//                .approverName(
//                        request.getApprover() != null
//                                ? request.getApprover().getFullName()
//                                : null)
//                .managerComments(request.getManagerComments())
//                .actionDate(request.getActionDate())
//                .createdAt(request.getCreatedAt())
//                .build();
//    }
    
    private LeaveResponseDto mapToResponseDto(
            LeaveRequest request) {

        LeaveResponseDto dto = LeaveResponseDto.builder()
                .id(request.getId())
                .employeeId(request.getUser().getId())
                .employeeName(request.getUser().getFullName())
                .employeeEmail(request.getUser().getEmail())
                .leaveType(request.getLeaveType().name())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .durationType(
                        request.getDurationType() != null
                                ? request.getDurationType().name()
                                : LeaveDurationType.FULL_DAY.name()
                )
                .reason(request.getReason())
                .status(request.getStatus().name())
                .approverId(
                        request.getApprover() != null
                                ? request.getApprover().getId()
                                : null)
                .approverName(
                        request.getApprover() != null
                                ? request.getApprover().getFullName()
                                : null)
                .managerComments(request.getManagerComments())

                .actionDate(request.getActionDate())

                .createdAt(request.getCreatedAt())

                .slaStatus(
                        request.getSlaStatus() != null
                                ? request.getSlaStatus().name()
                                : null
                )

                .slaDueAt(
                        request.getSlaDueAt()
                )
                
                .backupPersonId(
                	    request.getBackupPerson() != null
                	        ? request.getBackupPerson().getId()
                	        : null
                	)
                	.backupPersonName(
                	    request.getBackupPerson() != null
                	        ? request.getBackupPerson().getFullName()
                	        : null
                	)

                .build();

        LeaveDocumentRequest latestRequest =
                leaveDocumentRequestRepository
                        .findFirstByLeaveRequestIdOrderByRequestedAtDesc(
                                request.getId()
                        )
                        .orElse(null);

        if (latestRequest != null) {

            dto.setDocumentRequested(true);

            dto.setDocumentStatus(
                    latestRequest.getDocumentStatus()
            );

            dto.setDocumentPriority(
                    latestRequest.getPriority()
            );

        } else {

            dto.setDocumentRequested(false);

        }

        return dto;
    }
    
//    @Transactional(readOnly = true)
//    public List<LeaveResponseDto> getManagerPendingRequests(
//            Long managerId) {
//
//        return leaveRequestRepository
//                .findByUserManagerIdAndStatusOrderByCreatedAtDesc(
//                        managerId,
//                        LeaveStatus.PENDING
//                )
//                .stream()
//                .map(this::mapToResponseDto)
//                .toList();
//    }
    
    // 2 APPROVAL FLOW//
    

@Transactional(readOnly = true)
public List<LeaveResponseDto> getManagerPendingRequests(
        Long managerId) {

    return leaveRequestRepository
            .findPendingRequestsForApprover(
                    managerId,
                    LeaveStatus.PENDING
            )
            .stream()
            .map(this::mapToResponseDto)
            .toList();
}


    @Transactional(readOnly = true)
    public List<LeaveRequest> getAllRequests() {

        return leaveRequestRepository.findAll();
    }
    
    
    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getAllRequestResponses() {

        return leaveRequestRepository
                .findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }
//    public LeaveResponseDto processLeaveAction(
//            Long requestId,
//            LeaveStatus requestedStatus,
//            Long managerId,
//            String managerComments) {
//
//        if (requestedStatus != LeaveStatus.APPROVED
//                && requestedStatus != LeaveStatus.REJECTED) {
//
//            throw new InvalidWorkflowActionException(
//                    "Only APPROVED or REJECTED actions are allowed"
//            );
//        }
//
//        LeaveRequest request =
//                leaveRequestRepository
//                        .findById(requestId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Leave request not found : "
//                                                + requestId));
//
//        if (request.getStatus() != LeaveStatus.PENDING) {
//
//            throw new InvalidWorkflowActionException(
//                    "Leave request has already been processed"
//            );
//        }
//
//        User manager =
//                userRepository
//                        .findById(managerId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Manager not found : "
//                                                + managerId));
//
//        validateManagerAuthorization(
//                request,
//                manager
//        );
//
//        User employee =
//                request.getUser();
//
//        request.setStatus(requestedStatus);
//        request.setApprover(manager);
//        request.setManagerComments(managerComments);
//        request.setActionDate(LocalDateTime.now());
//        request.setUpdatedAt(LocalDateTime.now());
//
//        if (requestedStatus == LeaveStatus.APPROVED) {
//
//            processApproval(
//                    request,
//                    employee,
//                    manager
//            );
//
//        } else {
//
//            processRejection(
//                    request,
//                    employee,
//                    manager
//            );
//        }
//
//        LeaveRequest updatedRequest =
//                leaveRequestRepository.save(request);
//
//        return convertToResponse(updatedRequest);
//    }
    
    //2 APPROVAL FLOW//
    
    @Transactional
    public LeaveResponseDto processLeaveAction(
            Long requestId,
            LeaveStatus requestedStatus,
            Long managerId,
            String managerComments) {

        if (requestedStatus != LeaveStatus.APPROVED
                && requestedStatus != LeaveStatus.REJECTED) {
            throw new InvalidWorkflowActionException(
                    "Only APPROVED or REJECTED actions are allowed"
            );
        }

        LeaveRequest request = leaveRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found : " + requestId
                ));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidWorkflowActionException(
                    "Leave request has already been processed"
            );
        }

        User approver = userRepository
                .findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Approver not found : " + managerId
                ));

        validateManagerAuthorization(request, approver);

        User employee = request.getUser();

        int currentLevel = request.getCurrentApprovalLevel() == null
                ? 1
                : request.getCurrentApprovalLevel();

        request.setApprover(approver);
        request.setManagerComments(managerComments);
        request.setActionDate(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        if (requestedStatus == LeaveStatus.REJECTED) {

            // Rejection at either level ends the workflow.
            request.setStatus(LeaveStatus.REJECTED);

            leaveActionTokenService.invalidateUnusedTokens(
                    request.getId(),
                    approver.getId()
            );

            processRejection(request, employee, approver);

        } else if (currentLevel == 1
                && Integer.valueOf(2).equals(employee.getApprovalLevels())) {

            // Invalidate Level 1 tokens before advancing.
            leaveActionTokenService.invalidateUnusedTokens(
                    request.getId(),
                    approver.getId()
            );

            User level2Approver = employee.getLevel2Approver();

            if (level2Approver == null) {
                throw new InvalidWorkflowActionException(
                        "Level 2 approver is not configured"
                );
            }

            // Advance to Level 2, keeping the request PENDING.
            request.setCurrentApprovalLevel(2);

            notificationService.createNotification(
                    level2Approver.getId(),
                    "Leave Request Pending Approval",
                    employee.getFullName()
                            + "'s leave request requires your Level 2 approval."
            );

            LeaveRequest updatedRequest =
                    leaveRequestRepository.save(request);

            // Generate fresh tokens for Level 2.
            Map<LeaveActionType, String> actionTokens =
                    leaveActionTokenService.createActionTokens(
                            updatedRequest,
                            level2Approver,
                            updatedRequest.getSlaDueAt()
                    );

            String approveUrl =
                    frontendUrl + "/leave-action?token="
                            + actionTokens.get(LeaveActionType.APPROVE);

            String rejectUrl =
                    frontendUrl + "/leave-action?token="
                            + actionTokens.get(LeaveActionType.REJECT);

            String requestDocumentUrl =
                    frontendUrl + "/leave-action?token="
                            + actionTokens.get(
                                    LeaveActionType.REQUEST_DOCUMENT
                            );

            Map<String, String> emailVariables = new HashMap<>();

            emailVariables.put(
                    "requestId",
                    updatedRequest.getId().toString()
            );
            emailVariables.put(
                    "managerName",
                    level2Approver.getFullName()
            );
            emailVariables.put(
                    "employeeName",
                    employee.getFullName()
            );
            emailVariables.put(
                    "leaveType",
                    updatedRequest.getLeaveType().name()
            );
            emailVariables.put(
                    "duration",
                    getDurationDisplay(updatedRequest)
            );
            emailVariables.put(
                    "startDate",
                    updatedRequest.getStartDate().toString()
            );
            emailVariables.put(
                    "endDate",
                    updatedRequest.getEndDate().toString()
            );
            emailVariables.put(
                    "totalDays",
                    updatedRequest.getTotalDays().toString()
            );
            emailVariables.put(
                    "reason",
                    updatedRequest.getReason() != null
                            ? updatedRequest.getReason()
                            : ""
            );
            emailVariables.put("approveUrl", approveUrl);
            emailVariables.put("rejectUrl", rejectUrl);
            emailVariables.put("requestDocumentUrl", requestDocumentUrl);

            emailService.sendTemplateEmail(
                    level2Approver.getEmail(),
                    "LEAVE_MANAGER_ACTION",
                    emailVariables
            );

            log.info(
                    "Level 1 approved. Waiting for Level 2. RequestId={}, Level2ApproverId={}",
                    updatedRequest.getId(),
                    level2Approver.getId()
            );

            return convertToResponse(updatedRequest);

        } else {

            // One-level approval or final Level 2 approval.
            request.setStatus(LeaveStatus.APPROVED);

            leaveActionTokenService.invalidateUnusedTokens(
                    request.getId(),
                    approver.getId()
            );

            processApproval(request, employee, approver);
        }

        LeaveRequest updatedRequest = leaveRequestRepository.save(request);

        return convertToResponse(updatedRequest);
    }
    

    
    private void processApproval(
            LeaveRequest request,
            User employee,
            User manager) {

        LeaveBalance balance =
                leaveBalanceRepository
                        .findByUserId(employee.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave balance not found"
                                ));

//        int availableBalance =
//                balance.getAnnualBalance()
//                        - balance.getUsedLeave();
        
        BigDecimal availableBalance =
                balance.getAnnualBalance()
                        .subtract(balance.getUsedLeave());

//        if (availableBalance
//                < request.getTotalDays()) {
//
//            throw new InsufficientBalanceException(
//                    "Insufficient balance during approval. "
//                            + "Available="
//                            + availableBalance
//                            + ", Requested="
//                            + request.getTotalDays()
//            );
//        }
        
        if (availableBalance.compareTo(request.getTotalDays()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance during approval. "
                            + "Available=" + availableBalance
                            + ", Requested=" + request.getTotalDays()
            );
        }

//        balance.setUsedLeave(
//                balance.getUsedLeave()
//                        + request.getTotalDays()
//        );
        
        balance.setUsedLeave(
                balance.getUsedLeave()
                        .add(request.getTotalDays())
        );

//        balance.setRemainingLeave(
//                balance.getAnnualBalance()
//                        - balance.getUsedLeave()
//        );
        
        balance.setRemainingLeave(
                balance.getAnnualBalance()
                        .subtract(balance.getUsedLeave())
        );

        balance.setUpdatedAt(
                LocalDateTime.now()
        );

        leaveBalanceRepository.save(balance);

        auditService.logAction(
                "LEAVE_APPROVED",
                manager,
                "LEAVE_REQUEST",
                request.getId(),
                "Leave approved for employee "
                        + employee.getFullName()
        );

        emailService.sendEmployeeApproval(
                employee,
                manager,
                request
        );
        notificationService.createNotification(
                employee.getId(),
                "Leave Approved",
                "Manager approved your leave request."
        );

        log.info(
                "Leave approved. RequestId={}, EmployeeId={}",
                request.getId(),
                employee.getId()
        );
    }
    
    private void processRejection(
            LeaveRequest request,
            User employee,
            User manager) {

        auditService.logAction(
                "LEAVE_REJECTED",
                manager,
                "LEAVE_REQUEST",
                request.getId(),
                "Leave rejected for employee "
                        + employee.getFullName()
        );

//        emailService.sendEmployeeRejection(
//                employee,
//                manager
//        );
        
        emailService.sendEmployeeRejection(
                employee,
                manager,
                request
        );
        
        notificationService.createNotification(
                employee.getId(),
                "Leave Rejected",
                "Manager rejected your leave request."
        );

        log.info(
                "Leave rejected. RequestId={}, EmployeeId={}",
                request.getId(),
                employee.getId()
        );
    }
    
//    private void validateManagerAuthorization(
//            LeaveRequest request,
//            User manager) {
//
//        User employee =
//                request.getUser();
//
//        if (employee.getManager() == null) {
//
//            throw new InvalidWorkflowActionException(
//                    "Employee has no assigned manager"
//            );
//        }
//
//        Long assignedManagerId =
//                employee.getManager().getId();
//
//        if (!assignedManagerId.equals(manager.getId())) {
//
//            throw new InvalidWorkflowActionException(
//                    "You are not authorized to process "
//                            + "this leave request"
//            );
//        }
//    }
    
    //2 APPROVAL FLOW//
    

private void validateManagerAuthorization(
        LeaveRequest request,
        User manager) {

    User employee = request.getUser();

    Integer currentLevel = request.getCurrentApprovalLevel();

    if (currentLevel == null) {
        currentLevel = 1;
    }

    User expectedApprover;

    if (currentLevel == 1) {
        expectedApprover = employee.getLevel1Approver();
    } else if (currentLevel == 2
            && Integer.valueOf(2).equals(employee.getApprovalLevels())) {
        expectedApprover = employee.getLevel2Approver();
    } else {
        throw new InvalidWorkflowActionException(
                "Invalid approval level for this leave request"
        );
    }

    if (expectedApprover == null) {
        throw new InvalidWorkflowActionException(
                "No approver configured for approval level " + currentLevel
        );
    }

    if (!Boolean.TRUE.equals(manager.getActive())) {
        throw new InvalidWorkflowActionException(
                "Inactive approver cannot process leave requests"
        );
    }

    if (!expectedApprover.getId().equals(manager.getId())) {
        throw new InvalidWorkflowActionException(
                "You are not authorized to process this leave request at Level "
                        + currentLevel
        );
    }
}

    @Transactional(readOnly = true)
    public LeaveBalance getEmployeeBalance(
            Long userId) {

        return leaveBalanceRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave balance not found"
                        ));
    }
    
    @Transactional(readOnly = true)
    public long getTotalRequests() {

        return leaveRequestRepository.count();
    }

    @Transactional(readOnly = true)
    public long getApprovedRequests() {

        return leaveRequestRepository.countByStatus(
                LeaveStatus.APPROVED
        );
    }

    @Transactional(readOnly = true)
    public long getRejectedRequests() {

        return leaveRequestRepository.countByStatus(
                LeaveStatus.REJECTED
        );
    }

    @Transactional(readOnly = true)
    public long getPendingRequests() {

        return leaveRequestRepository.countByStatus(
                LeaveStatus.PENDING
        );
        
        
    }
    
    public void cancelLeave(
            Long leaveId,
            Long employeeId) {

        LeaveRequest leave =
                leaveRequestRepository
                        .findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave not found"));

        if (!leave.getUser()
                .getId()
                .equals(employeeId)) {

            throw new RuntimeException(
                    "Unauthorized");
        }

        if (leave.getStatus()
                != LeaveStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending leave can be cancelled");
        }

        leave.setStatus(
                LeaveStatus.CANCELLED);

        leave.setUpdatedAt(
                LocalDateTime.now());

        leaveRequestRepository.save(
                leave);

        notificationService.createNotification(
                employeeId,
                "Leave Cancelled",
                "Your leave request has been cancelled."
        );

        if (leave.getUser().getManager() != null) {

            notificationService.createNotification(
                    leave.getUser()
                            .getManager()
                            .getId(),
                    "Leave Cancelled",
                    leave.getUser()
                            .getFullName()
                            + " cancelled leave request."
            );
        }

        auditService.logAction(
                "LEAVE_CANCELLED",
                leave.getUser(),
                "LEAVE_REQUEST",
                leave.getId(),
                "Employee cancelled leave request"
        );
    }
    
    @Transactional(readOnly = true)
    public List<LeaveResponseDto>
    getEmployeesOnLeaveToday() {

        return leaveRequestRepository
                .findEmployeesOnLeaveToday()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }
    
    public List<CalendarEventDto>
    getManagerCalendar() {

        List<CalendarEventDto> events =
                new ArrayList<>();

        leaveRequestRepository
                .findByStatus(
                        LeaveStatus.APPROVED
                )
                .forEach(leave ->

                    events.add(

                        CalendarEventDto
                                .builder()

                                .title(
                                        leave.getUser()
                                                .getFullName()
                                                + " - "
                                                + leave.getLeaveType()
                                )

                                .start(
                                        leave.getStartDate()
                                )

                                .end(
                                        leave.getEndDate()
                                )

                                .type(
                                        "LEAVE"
                                )

                                .build()
                    )
                );
        
        // Holiday Events
        holidayRepository
                .findAll()
                .forEach(holiday ->

                    events.add(

                        CalendarEventDto
                                .builder()
                                .title(
                                        holiday.getHolidayName()
                                )
                                .start(
                                        holiday.getHolidayDate()
                                )
                                .end(
                                        holiday.getHolidayDate()
                                )
                                .type(
                                        "HOLIDAY"
                                )
                                .build()
                    )
                );

        return events;
    }
    
//    @Transactional(readOnly = true)
//    public List<LeaveResponseDto> getManagerTeamRequests(
//            Long managerId) {
//
//        return leaveRequestRepository
//                .findAll()
//                .stream()
//                .filter(request ->
//                        request.getUser() != null
//                        && request.getUser().getManager() != null
//                        && request.getUser()
//                                .getManager()
//                                .getId()
//                                .equals(managerId)
//                )
//                .map(this::mapToResponseDto)
//                .toList();
//    }
    

@Transactional(readOnly = true)
public List<LeaveResponseDto> getManagerTeamRequests(Long managerId) {

    return leaveRequestRepository
            .findAll()
            .stream()
            .filter(request -> {
                if (request.getUser() == null) {
                    return false;
                }

                var employee = request.getUser();

                boolean directManager =
                        employee.getManager() != null
                        && employee.getManager().getId().equals(managerId);

                boolean level1Approver =
                        employee.getLevel1Approver() != null
                        && employee.getLevel1Approver().getId().equals(managerId);

                boolean level2Approver =
                        employee.getLevel2Approver() != null
                        && employee.getLevel2Approver().getId().equals(managerId);

                return directManager || level1Approver || level2Approver;
            })
            .map(this::mapToResponseDto)
            .toList();
}

    
    private String getDurationDisplay(LeaveRequest request) {

        if (request.getDurationType() == null) {
            return "Full Day";
        }

        return switch (request.getDurationType()) {
            case FIRST_HALF -> "First Half (Morning)";
            case SECOND_HALF -> "Second Half (Afternoon)";
            case FULL_DAY -> "Full Day";
        };
    }
    
    @Transactional(readOnly = true)
    public List<BackupPersonDto> getBackupPersons(Long userId) {

        return userCountryMappingRepository
                .findBackupPersons(userId)
                .stream()
                .map(user -> BackupPersonDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .toList();
    }
   
}

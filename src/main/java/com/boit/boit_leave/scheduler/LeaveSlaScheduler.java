package com.boit.boit_leave.scheduler;


import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.LeaveStatus;
import com.boit.boit_leave.entity.SlaStatus;
import com.boit.boit_leave.dto.SlaConfigurationDto;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.LeaveRequestRepository;
import com.boit.boit_leave.service.EmailService;
import com.boit.boit_leave.service.SlaConfigurationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveSlaScheduler {

    private static final String WARNING_TEMPLATE =
            "LEAVE_SLA_WARNING";

    private static final String BREACHED_TEMPLATE =
            "LEAVE_SLA_BREACHED";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LeaveRequestRepository leaveRequestRepository;
    private final SlaConfigurationService slaConfigurationService;
    private final EmailService emailService;

    /**
     * Runs every 15 minutes.
     *
     * Checks:
     * 1. SLA warning candidates
     * 2. SLA breached requests
     * 900000 means 15 min
     * for testing we are setting as 1 min which is 60000
     */
    @Scheduled(fixedDelay = 900000)
    @Transactional
    public void processLeaveSla() {

        LocalDateTime now = LocalDateTime.now();

        log.info("Starting Leave SLA check at {}", now);

        processWarnings(now);
        processBreaches(now);

        log.info("Leave SLA check completed at {}", LocalDateTime.now());
    }

    /**
     * Sends SLA warning email before the configured SLA deadline.
     */
    private void processWarnings(LocalDateTime now) {

    	SlaConfigurationDto config =
    	        slaConfigurationService.getConfiguration();

        int warningBefore = config.getWarningBefore();

        List<LeaveRequest> pendingRequests =
                leaveRequestRepository
                        .findByStatusAndSlaDueAtIsNotNullAndSlaWarningSentFalse(
                                LeaveStatus.PENDING
                        );

        for (LeaveRequest request : pendingRequests) {

            if (request.getSlaDueAt() == null) {
                continue;
            }

            LocalDateTime warningTime =
                    calculateWarningTime(
                            request.getSlaDueAt(),
                            config,
                            warningBefore
                    );

            if (!now.isBefore(warningTime)
                    && now.isBefore(request.getSlaDueAt())) {

                sendWarningEmail(request);

                request.setSlaWarningSent(true);
                request.setSlaStatus(SlaStatus.WARNING);

                leaveRequestRepository.save(request);

                log.info(
                        "SLA warning processed for leave request ID {}",
                        request.getId()
                );
            }
        }
    }

    /**
     * Sends SLA breach email after the SLA deadline.
     */
    private void processBreaches(LocalDateTime now) {
    	

        long breachCount =
                leaveRequestRepository.countBreachedSlaRequests();

        log.info(
                "DATABASE BREACH COUNT = {}",
                breachCount
        );

        List<LeaveRequest> breachedRequests =
                leaveRequestRepository
                        .findBreachedSlaRequests(now);

        log.info(
                "SLA breach candidates found: {}",
                breachedRequests.size()
        );

        for (LeaveRequest request : breachedRequests) {

            log.info(
                    "Processing SLA breach for request ID {} | dueAt={} | status={} | breachEmailSent={}",
                    request.getId(),
                    request.getSlaDueAt(),
                    request.getStatus(),
                    request.getSlaBreachEmailSent()
            );

            sendBreachEmail(request);

            request.setSlaBreachEmailSent(true);
            request.setSlaStatus(SlaStatus.BREACHED);

            leaveRequestRepository.save(request);

            log.info(
                    "SLA breach processed successfully for leave request ID {}",
                    request.getId()
            );
        }
    }

    /**
     * Calculates when the warning should start.
     *
     * Currently supports:
     * DAYS
     * HOURS
     */
    private LocalDateTime calculateWarningTime(
            LocalDateTime slaDueAt,
            SlaConfigurationDto config,
            int warningBefore) {

        if ("HOURS".equalsIgnoreCase(config.getSlaUnit())) {

            return slaDueAt.minusHours(warningBefore);

        }

        return slaDueAt.minusDays(warningBefore);
    }

    /**
     * Sends the configurable SLA warning template
     * to the employee's manager.
     */
    private void sendWarningEmail(LeaveRequest request) {

        User employee = request.getUser();

        if (employee == null) {
            log.warn(
                    "Skipping SLA warning for leave request {} - employee not found",
                    request.getId()
            );
            return;
        }

        User manager = employee.getManager();

        if (manager == null || manager.getEmail() == null
                || manager.getEmail().isBlank()) {

            log.warn(
                    "Skipping SLA warning for leave request {} - manager/email not found",
                    request.getId()
            );
            return;
        }

        Map<String, String> variables =
                buildTemplateVariables(request, employee, manager);

        emailService.sendTemplateEmail(
                manager.getEmail(),
                WARNING_TEMPLATE,
                variables
        );
    }

    /**
     * Sends the configurable SLA breach template
     * to the employee's manager.
     */
    private void sendBreachEmail(LeaveRequest request) {

        User employee = request.getUser();

        if (employee == null) {
            log.warn(
                    "Skipping SLA breach for leave request {} - employee not found",
                    request.getId()
            );
            return;
        }

        User manager = employee.getManager();

        if (manager == null || manager.getEmail() == null
                || manager.getEmail().isBlank()) {

            log.warn(
                    "Skipping SLA breach for leave request {} - manager/email not found",
                    request.getId()
            );
            return;
        }

        Map<String, String> variables =
                buildTemplateVariables(request, employee, manager);

        emailService.sendTemplateEmail(
                manager.getEmail(),
                BREACHED_TEMPLATE,
                variables
        );
    }

    /**
     * Creates variables used by Notification Template Management.
     */
    private Map<String, String> buildTemplateVariables(
            LeaveRequest request,
            User employee,
            User manager) {

        Map<String, String> variables =
                new HashMap<>();

        variables.put(
                "managerName",
                manager.getFullName()
        );

        variables.put(
                "employeeName",
                employee.getFullName()
        );

        variables.put(
                "leaveType",
                request.getLeaveType() != null
                        ? request.getLeaveType().toString()
                        : ""
        );

        variables.put(
                "duration",
                getDurationDisplay(request)
        );

        variables.put(
                "startDate",
                request.getStartDate() != null
                        ? request.getStartDate().toString()
                        : ""
        );

        variables.put(
                "endDate",
                request.getEndDate() != null
                        ? request.getEndDate().toString()
                        : ""
        );

        variables.put(
                "totalDays",
                request.getTotalDays() != null
                        ? request.getTotalDays().toString()
                        : ""
        );

        variables.put(
                "slaDueAt",
                request.getSlaDueAt() != null
                        ? request.getSlaDueAt()
                            .format(DATE_TIME_FORMATTER)
                        : ""
        );

        return variables;
    }

    /**
     * Displays Full Day / First Half / Second Half.
     */
    private String getDurationDisplay(LeaveRequest request) {

        if (request.getDurationType() == null) {
            return "Full Day";
        }

        return switch (request.getDurationType()) {

            case FIRST_HALF ->
                    "First Half (Morning)";

            case SECOND_HALF ->
                    "Second Half (Afternoon)";

            case FULL_DAY ->
                    "Full Day";
        };
    }
}
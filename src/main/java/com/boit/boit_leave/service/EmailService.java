package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationTemplateService notificationTemplateService;

@Async
public void sendManagerNotification(
        String managerEmail,
        String employeeName,
        LeaveRequest request) {

    Map<String, String> variables = Map.of(
            "employeeName", employeeName == null ? "" : employeeName,
            "leaveType", request.getLeaveType() == null
                    ? "" : request.getLeaveType().toString(),
            "duration", getDurationDisplay(request),
            "startDate", request.getStartDate() == null
                    ? "" : request.getStartDate().toString(),
            "endDate", request.getEndDate() == null
                    ? "" : request.getEndDate().toString(),
            "totalDays", request.getTotalDays() == null
                    ? "" : request.getTotalDays().toString(),
            "reason", request.getReason() == null
                    ? "" : request.getReason()
    );

    sendTemplateEmail(
            managerEmail,
            "LEAVE_REQUEST_MANAGER_NOTIFICATION",
            variables
    );
}



@Async
public void sendEmployeeApproval(
        User employee,
        User manager,
        LeaveRequest request) {

    Map<String, String> variables = Map.of(
            "employeeName", templateValue(employee.getFullName()),
            "leaveType", templateValue(request.getLeaveType()),
            "startDate", templateValue(request.getStartDate()),
            "endDate", templateValue(request.getEndDate()),
            "totalDays", templateValue(request.getTotalDays()),
            "managerName", templateValue(manager.getFullName())
    );

    sendTemplateEmail(
            employee.getEmail(),
            "LEAVE_REQUEST_APPROVED",
            variables
    );
}



@Async
public void sendEmployeeRejection(
        User employee,
        User manager,
        LeaveRequest request) {

    String comments = request.getManagerComments() == null
            ? "No comments provided"
            : request.getManagerComments();

    Map<String, String> variables = Map.of(
            "employeeName", templateValue(employee.getFullName()),
            "leaveType", templateValue(request.getLeaveType()),
            "startDate", templateValue(request.getStartDate()),
            "endDate", templateValue(request.getEndDate()),
            "totalDays", templateValue(request.getTotalDays()),
            "managerName", templateValue(manager.getFullName()),
            "comments", comments
    );

    sendTemplateEmail(
            employee.getEmail(),
            "LEAVE_REQUEST_REJECTED",
            variables
    );
}

    private void sendEmail(
            String to,
            String subject,
            String body) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false
                    );
            helper.setFrom("mujeeburrahman3507@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

            log.info(
                    "Email sent successfully to {}",
                    to
            );

        } catch (
                MessagingException |
                MailException ex) {

            log.error(
                    "Email sending failed : {}",
                    ex.getMessage(),
                    ex
            );
        }
    }
    

@Async
public void sendOtpEmail(
        String email,
        String fullName,
        String otp) {

    Map<String, String> variables = Map.of(
            "fullName", templateValue(fullName),
            "otp", templateValue(otp)
    );

    sendTemplateEmail(
            email,
            "PASSWORD_RESET_OTP",
            variables
    );
}


@Async
public void sendHolidayReminderEmail(
        String email,
        String holidayName) {

    Map<String, String> variables = Map.of(
            "holidayName", templateValue(holidayName)
    );

    sendTemplateEmail(
            email,
            "HOLIDAY_REMINDER",
            variables
    );
}



@Async
public void sendDocumentRequestEmail(
        String email,
        String employeeName,
        String documentType,
        String managerName,
        String comments) {

    Map<String, String> variables = Map.of(
            "employeeName", templateValue(employeeName),
            "documentType", documentType == null
                    ? "" : documentType.replace("_", " "),
            "managerName", templateValue(managerName),
            "comments", templateValue(comments)
    );

    sendTemplateEmail(
            email,
            "DOCUMENT_REQUESTED",
            variables
    );
}

    

public void sendDocumentVerifiedEmail(
        String employeeEmail,
        String employeeName,
        String documentType,
        String managerName) {

    Map<String, String> variables = Map.of(
            "employeeName", templateValue(employeeName),
            "documentType", documentType == null
                    ? "" : documentType.replace("_", " "),
            "managerName", templateValue(managerName)
    );

    sendTemplateEmail(
            employeeEmail,
            "DOCUMENT_VERIFIED",
            variables
    );
}


public void sendDocumentRejectedEmail(
        String employeeEmail,
        String employeeName,
        String documentType,
        String managerName,
        String comments) {

    Map<String, String> variables = Map.of(
            "employeeName", templateValue(employeeName),
            "documentType", documentType == null
                    ? "" : documentType.replace("_", " "),
            "managerName", templateValue(managerName),
            "comments", templateValue(comments)
    );

    sendTemplateEmail(
            employeeEmail,
            "DOCUMENT_REJECTED",
            variables
    );
}

    
    /**
     * Sends an email using a database-managed notification template.
     *
     * The template provides:
     * - email subject
     * - email HTML body
     *
     * Variables such as {employeeName}, {cycleName}, etc.
     * are replaced by NotificationTemplateService.
     */
    @Async
    public void sendTemplateEmail(
            String recipientEmail,
            String templateCode,
            Map<String, String> variables) {

        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn(
                    "Template email skipped because recipient email is empty. Template: {}",
                    templateCode
            );
            return;
        }

        try {

            String subject =
                    notificationTemplateService.resolveEmailSubject(
                            templateCode,
                            variables
                    );

            String body =
                    notificationTemplateService.resolveEmailBody(
                            templateCode,
                            variables
                    );

            if (subject == null || subject.isBlank()) {
                log.warn(
                        "Template email skipped because subject is empty. Template: {}",
                        templateCode
                );
                return;
            }

            if (body == null || body.isBlank()) {
                log.warn(
                        "Template email skipped because email body is empty. Template: {}",
                        templateCode
                );
                return;
            }

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

            log.info(
                    "Template email sent successfully. Template: {}, Recipient: {}",
                    templateCode,
                    recipientEmail
            );

        } catch (MessagingException | MailException ex) {

            log.error(
                    "Template email sending failed. Template: {}, Recipient: {}",
                    templateCode,
                    recipientEmail,
                    ex
            );

        } catch (Exception ex) {

            log.error(
                    "Unexpected error while sending template email. Template: {}, Recipient: {}",
                    templateCode,
                    recipientEmail,
                    ex
            );
        }
    }
    
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
    

private String templateValue(Object value) {
    return value == null ? "" : value.toString();
}

}
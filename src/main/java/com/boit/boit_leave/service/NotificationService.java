package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.Notification;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.NotificationRepository;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.entity.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.boit.boit_leave.dto.NotificationDto;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final NotificationTemplateService notificationTemplateService;
	private final EmailService emailService;
	
    /**
     * Sends both an in-app notification and an email
     * using the same database-managed template.
     */
	public void sendTemplateNotificationAndEmail(
	        Long userId,
	        String templateCode,
	        Map<String, String> variables) {

	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    NotificationTemplate template =
	            notificationTemplateService.getActiveTemplate(templateCode);

	    String channel = template.getChannel();

	    // IN_APP or BOTH
	    if ("IN_APP".equalsIgnoreCase(channel)
	            || "BOTH".equalsIgnoreCase(channel)) {

	        createTemplateNotification(
	                userId,
	                templateCode,
	                variables
	        );
	    }

	    // EMAIL or BOTH
	    if ("EMAIL".equalsIgnoreCase(channel)
	            || "BOTH".equalsIgnoreCase(channel)) {

	        if (user.getEmail() != null
	                && !user.getEmail().isBlank()) {

	            emailService.sendTemplateEmail(
	                    user.getEmail(),
	                    templateCode,
	                    variables
	            );
	        }
	    }
	}

    public void createNotification(
            Long userId,
            String title,
            String message) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Notification notification =
                Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        notificationRepository.save(notification);
    }

    public List<NotificationDto>
    getNotifications(Long userId) {

    	return notificationRepository
    	        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
    	                userId
    	        )
                .stream()
                .map(notification ->
                        NotificationDto.builder()
                                .id(notification.getId())
                                .title(notification.getTitle())
                                .message(notification.getMessage())
                                .isRead(notification.getIsRead())
                                .createdAt(notification.getCreatedAt())
                                .build()
                )
                .toList();
    }
    
    public void markAllAsRead(
            Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndIsReadFalse(
                                userId
                        );

        notifications.forEach(
                notification ->
                        notification.setIsRead(true)
        );

        notificationRepository.saveAll(
                notifications
        );
    }
    


    public long unreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndIsReadFalse(
                        userId
                );
    }

    public void markAsRead(Long id) {

        Notification notification =
                notificationRepository
                        .findById(id)
                        .orElseThrow();

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }
    
    public void createBirthdayNotification(User user) {

        Notification notification =
                Notification.builder()
                        .user(user)
                        .title("🎂 Happy Birthday!")
                        .message(
                                "Happy Birthday "
                                        + user.getFullName()
                                        + "! 🎉\n\n"
                                        + "The entire BOIT GLOBAL family wishes you "
                                        + "a wonderful year filled with happiness, "
                                        + "good health and success.\n\n"
                                        + "Have an amazing day! 🎈"
                        )
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        notificationRepository.save(notification);

    }
    
    // =========================================================
    // APPRAISAL NOTIFICATIONS
    // =========================================================

    public void notifyAppraisalCreated(
            Long employeeId,
            String cycleName) {

        createTemplateNotification(
                employeeId,
                "APPRAISAL_CREATED",
                Map.of(
                        "cycleName", cycleName
                )
        );
    }

    public void notifyGoalsSubmitted(
            Long managerId,
            String employeeName,
            String cycleName) {

        createTemplateNotification(
                managerId,
                "APPRAISAL_GOALS_SUBMITTED",
                Map.of(
                        "employeeName", employeeName,
                        "cycleName", cycleName
                )
        );
    }

    public void notifyGoalsApproved(
            Long employeeId,
            String cycleName) {

        createTemplateNotification(
                employeeId,
                "APPRAISAL_GOALS_APPROVED",
                Map.of(
                        "cycleName", cycleName
                )
        );
    }

    public void notifyGoalRevisionRequested(
            Long employeeId,
            String cycleName,
            String comment) {

        createTemplateNotification(
                employeeId,
                "APPRAISAL_GOAL_REVISION",
                Map.of(
                        "cycleName", cycleName,
                        "managerComment",
                        comment == null ? "" : comment
                )
        );
    }

    public void notifySelfReviewStarted(
            Long employeeId,
            String cycleName) {

        createTemplateNotification(
                employeeId,
                "APPRAISAL_SELF_REVIEW_STARTED",
                Map.of(
                        "cycleName", cycleName
                )
        );
    }

    public void notifySelfReviewSubmitted(
            Long managerId,
            String employeeName,
            String cycleName) {

        createTemplateNotification(
                managerId,
                "APPRAISAL_SELF_REVIEW_SUBMITTED",
                Map.of(
                        "employeeName", employeeName,
                        "cycleName", cycleName
                )
        );
    }

    public void notifyManagerReviewStarted(
            Long managerId,
            String employeeName,
            String cycleName) {

        createTemplateNotification(
                managerId,
                "APPRAISAL_MANAGER_REVIEW_STARTED",
                Map.of(
                        "employeeName", employeeName,
                        "cycleName", cycleName
                )
        );
    }

    public void notifyManagerReviewSubmitted(
            Long adminId,
            String employeeName,
            String cycleName) {

        createTemplateNotification(
                adminId,
                "APPRAISAL_MANAGER_REVIEW_SUBMITTED",
                Map.of(
                        "employeeName", employeeName,
                        "cycleName", cycleName
                )
        );
    }

    public void notifyHrReviewStarted(
            Long adminId,
            String employeeName,
            String cycleName) {

        createTemplateNotification(
                adminId,
                "APPRAISAL_HR_REVIEW_STARTED",
                Map.of(
                        "employeeName", employeeName,
                        "cycleName", cycleName
                )
        );
    }

    public void notifyAppraisalFinalized(
            Long employeeId,
            String cycleName) {

        createTemplateNotification(
                employeeId,
                "APPRAISAL_FINALIZED",
                Map.of(
                        "cycleName", cycleName
                )
        );
    }
    
    
    public void createTemplateNotification(
            Long userId,
            String templateCode,
            Map<String, String> variables) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        String title =
                notificationTemplateService.resolveTitle(
                        templateCode,
                        variables
                );

        String message =
                notificationTemplateService.resolveMessage(
                        templateCode,
                        variables
                );

        Notification notification =
                Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        notificationRepository.save(notification);
    }
}
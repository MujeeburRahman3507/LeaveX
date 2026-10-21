package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.BirthdayWishLog;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.BirthdayWishLogRepository;
import com.boit.boit_leave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BirthdayService {

    private final UserRepository userRepository;

    private final BirthdayWishLogRepository birthdayWishLogRepository;

    private final BirthdayEmailService birthdayEmailService;

    private final NotificationService notificationService;

    /**
     * Called by BirthdayScheduler every day.
     */
    public void processTodaysBirthdays() {

        log.info("Searching today's birthdays...");

        List<User> birthdayUsers =
                userRepository.findTodaysBirthdays();

        if (birthdayUsers.isEmpty()) {

            log.info("No birthdays found today.");

            return;
        }

        log.info("Found {} birthday employee(s).",
                birthdayUsers.size());

        for (User user : birthdayUsers) {

            try {

                boolean alreadySent =
                        birthdayWishLogRepository
                                .existsByUserIdAndWishDate(
                                        user.getId(),
                                        LocalDate.now()
                                );

                if (alreadySent) {

                    log.info(
                            "Birthday already processed for {}",
                            user.getEmployeeCode()
                    );

                    continue;
                }

                log.info(
                        "Processing birthday for {}",
                        user.getFullName()
                );

                /*
                 * Send Birthday Email
                 */
                birthdayEmailService
                        .sendBirthdayEmail(user);

                /*
                 * Create Notification
                 */
                notificationService
                        .createBirthdayNotification(user);

                /*
                 * Save Birthday Log
                 */
                BirthdayWishLog logEntry =
                        BirthdayWishLog.builder()

                                .user(user)

                                .wishDate(LocalDate.now())

                                .emailSent(true)

                                .notificationSent(true)

                                .createdAt(
                                        LocalDateTime.now()
                                )

                                .build();

                birthdayWishLogRepository
                        .save(logEntry);

                log.info(
                        "Birthday completed for {}",
                        user.getEmployeeCode()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed processing birthday for {}",
                        user.getEmployeeCode(),
                        ex
                );

            }

        }

    }

}
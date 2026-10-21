package com.boit.boit_leave.scheduler;

import com.boit.boit_leave.entity.Country;
import com.boit.boit_leave.entity.Holiday;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.entity.UserCountryMapping;
import com.boit.boit_leave.repository.HolidayRepository;
import com.boit.boit_leave.repository.UserCountryMappingRepository;
import com.boit.boit_leave.repository.UserRepository;
import com.boit.boit_leave.service.EmailService;
import com.boit.boit_leave.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayReminderScheduler {

    private final HolidayRepository holidayRepository;

    private final UserRepository userRepository;

    private final UserCountryMappingRepository
            userCountryMappingRepository;

    private final NotificationService
            notificationService;

    private final EmailService
            emailService;

//    @Scheduled(cron = "0 0 9 * * *")
//    @Scheduled(cron = "0 */1 * * * *")
    @Scheduled(cron = "${holiday.reminder.scheduler.cron}")
    public void sendHolidayReminder() {

        log.info(
                "Holiday Reminder Scheduler Started");

        LocalDate tomorrow =
                LocalDate.now()
                        .plusDays(1);

        List<Holiday> holidays =
                holidayRepository
                        .findByHolidayDate(
                                tomorrow);

        if(holidays.isEmpty()) {

            log.info(
                    "No Holidays Found");

            return;
        }

        for(Holiday holiday : holidays) {

            processHoliday(
                    holiday);
        }

        log.info(
                "Holiday Reminder Scheduler Completed");
    }

    private void processHoliday(
            Holiday holiday) {

        if(Boolean.TRUE.equals(
                holiday.getIsGlobal())) {

            sendGlobalHolidayReminder(
                    holiday);

        } else {

            sendCountryHolidayReminder(
                    holiday);
        }
    }

    private void sendGlobalHolidayReminder(
            Holiday holiday) {

        List<User> users =
                userRepository.findAll();

        users.forEach(user ->
                createNotification(
                        user,
                        holiday));
    }

    private void sendCountryHolidayReminder(
            Holiday holiday) {

        Country country =
                holiday.getCountry();

        List<UserCountryMapping>
                mappings =
                userCountryMappingRepository
                        .findByCountry(
                                country);

        mappings.forEach(mapping ->
                createNotification(
                        mapping.getUser(),
                        holiday));
    }

    private void createNotification(
            User user,
            Holiday holiday) {

        String title =
                "Holiday Reminder";

        String message =
                "Tomorrow is "
                        + holiday.getHolidayName()
                        + ". Enjoy your holiday!";

        try {

        	notificationService.createNotification(
        	        user.getId(),
        	        title,
        	        message
        	);

        	emailService.sendHolidayReminderEmail(
        	        user.getEmail(),
        	        holiday.getHolidayName()
        	);

            log.info(
                    "Holiday Reminder Sent To {}",
                    user.getEmail());

        } catch(Exception ex) {

            log.error(
                    "Failed Sending Reminder",
                    ex);
        }
    }
}
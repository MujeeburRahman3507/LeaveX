package com.boit.boit_leave.scheduler;

import com.boit.boit_leave.service.BirthdayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayScheduler {

    private final BirthdayService birthdayService;

    /**
     * Runs every day at 9:00 AM
     */
//    @Scheduled(cron = "0 0 9 * * *")
//  @Scheduled(cron = "0 */1 * * * *")
  @Scheduled(cron = "${birthday.scheduler.cron}")
    public void sendBirthdayWishes() {

        log.info("==============================================");
        log.info("Birthday Scheduler Started");
        log.info("==============================================");

        try {

            birthdayService.processTodaysBirthdays();

            log.info("Birthday Scheduler Completed Successfully");

        } catch (Exception ex) {

            log.error("Birthday Scheduler Failed", ex);

        }

        log.info("==============================================");

    }

}
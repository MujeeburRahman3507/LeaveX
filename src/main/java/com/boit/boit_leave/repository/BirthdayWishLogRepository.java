package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.BirthdayWishLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BirthdayWishLogRepository
        extends JpaRepository<BirthdayWishLog, Long> {

    /**
     * Checks whether a birthday wish
     * has already been sent today.
     */
    boolean existsByUserIdAndWishDate(
            Long userId,
            LocalDate wishDate
    );

}
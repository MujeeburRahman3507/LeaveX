package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository
        extends JpaRepository<Holiday, Long> {

    List<Holiday>
    findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(
            LocalDate date
    );
    
    @Query("""
    		SELECT h
    		FROM Holiday h
    		WHERE h.holidayDate >= :today
    		ORDER BY h.holidayDate ASC
    		""")
    		List<Holiday> findUpcomingHolidays(
    		        @Param("today")
    		        LocalDate today
    		);
    
    List<Holiday> findByHolidayDate(
            LocalDate holidayDate);
    
    List<Holiday> findByHolidayDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
    
    Long countByHolidayDateGreaterThanEqual(
            LocalDate date);
    
    // =====================================================
    // CSV IMPORT SUPPORT
    // =====================================================

    List<Holiday> findByCountryIdAndHolidayDateBetween(
            Long countryId,
            LocalDate startDate,
            LocalDate endDate
    );

    void deleteByCountryIdAndHolidayDateBetween(
            Long countryId,
            LocalDate startDate,
            LocalDate endDate
    );

}
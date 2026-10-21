package com.boit.boit_leave.service;

import com.boit.boit_leave.dto.HolidayDto;

import java.util.List;

public interface HolidayService {

    List<HolidayDto> getAll();

    HolidayDto create(
            HolidayDto dto);

    HolidayDto update(
            Long id,
            HolidayDto dto);

    void delete(
            Long id);
    
    List<HolidayDto> getEmployeeHolidays(
            Long userId);
}
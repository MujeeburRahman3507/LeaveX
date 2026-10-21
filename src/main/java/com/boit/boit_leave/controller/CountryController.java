package com.boit.boit_leave.controller;

import com.boit.boit_leave.dto.CountryDto;
import com.boit.boit_leave.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public List<CountryDto> getAllCountries() {

        return countryService.getAllCountries();
    }
}
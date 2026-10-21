package com.boit.boit_leave.service.impl;

import com.boit.boit_leave.dto.CountryDto;
import com.boit.boit_leave.repository.CountryRepository;
import com.boit.boit_leave.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl
        implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    public List<CountryDto> getAllCountries() {

        return countryRepository
                .findByActiveTrue()
                .stream()
                .map(country ->
                        CountryDto.builder()
                                .id(country.getId())
                                .countryCode(country.getCountryCode())
                                .countryName(country.getCountryName())
                                .build()
                )
                .toList();
    }
}
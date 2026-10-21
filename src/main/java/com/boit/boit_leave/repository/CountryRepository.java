package com.boit.boit_leave.repository;

import com.boit.boit_leave.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository
        extends JpaRepository<Country, Long> {

    List<Country> findByActiveTrue();

}
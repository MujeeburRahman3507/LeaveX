package com.boit.boit_leave.repository;

import com.boit.boit_leave.dto.CountryEmployeeCountDto;
import com.boit.boit_leave.entity.Country;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.entity.UserCountryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCountryMappingRepository
        extends JpaRepository<UserCountryMapping, Long> {

    List<UserCountryMapping>
    findByUser(User user);

    void deleteByUser(User user);

    List<UserCountryMapping>
    findByCountry(Country country);
    
    @Query("""
    	    SELECT new com.boit.boit_leave.dto.CountryEmployeeCountDto(
    	        c.countryName,
    	        COUNT(DISTINCT u.id)
    	    )
    	    FROM UserCountryMapping ucm
    	    JOIN ucm.user u
    	    JOIN ucm.country c
    	    GROUP BY c.countryName
    	    ORDER BY COUNT(DISTINCT u.id) DESC
    	    """)
    	    List<CountryEmployeeCountDto> getEmployeeCountByCountry();
    
    
    @Query("""
    	    SELECT DISTINCT u
    	    FROM UserCountryMapping ucm
    	    JOIN ucm.user u
    	    WHERE u.active = true
    	      AND u.id <> :userId
    	      AND ucm.country.id IN (
    	          SELECT ucm2.country.id
    	          FROM UserCountryMapping ucm2
    	          WHERE ucm2.user.id = :userId
    	      )
    	    ORDER BY u.fullName
    	    """)
    	List<User> findBackupPersons(
    	        @Param("userId") Long userId
    	);
}
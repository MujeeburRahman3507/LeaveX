package com.boit.boit_leave.repository;

import com.boit.boit_leave.dto.CountryEmployeeCountDto;
import com.boit.boit_leave.entity.Role;
import com.boit.boit_leave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    List<User> findByManagerId(Long managerId);

    List<User> findByRole(Role role);

    List<User> findByRoleAndActiveTrue(Role role);
    
    boolean existsByEmployeeCode(String employeeCode);
    
    long countByRole(Role role);
    
    Long countByActiveTrue();
    
    @Query(value = """
    		SELECT *
    		FROM users
    		WHERE EXTRACT(MONTH FROM date_of_birth) = EXTRACT(MONTH FROM CURRENT_DATE)
    		AND EXTRACT(DAY FROM date_of_birth) = EXTRACT(DAY FROM CURRENT_DATE)
    		AND active = true
    		ORDER BY full_name
    		""", nativeQuery = true)
    		List<User> findTodaysBirthdays();
    
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    
}
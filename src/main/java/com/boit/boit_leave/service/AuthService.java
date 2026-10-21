package com.boit.boit_leave.service;

import com.boit.boit_leave.config.JwtService;
import com.boit.boit_leave.dto.ChangePasswordDto;
import com.boit.boit_leave.dto.LoginRequestDto;
import com.boit.boit_leave.dto.LoginResponseDto;
import com.boit.boit_leave.dto.RegisterRequestDto;
import com.boit.boit_leave.dto.UserResponseDto;
import com.boit.boit_leave.entity.Role;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;


    public LoginResponseDto login(
            LoginRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow();

        String token =
                jwtService.generateToken(
                        org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities(
                                        List.of(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_" +
                                                                user.getRole().name()
                                                )
                                        )
                                )
                                .build()
                );

        return LoginResponseDto.builder()
                .token(token)
                .username(user.getFullName())
                .email(user.getEmail())
                .authorities(
                        List.of(
                                "ROLE_" + user.getRole().name()
                        )
                )
                .build();
    }

    public UserResponseDto register(
            RegisterRequestDto request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new RuntimeException(
                    "Email already exists");
        }

        if (userRepository.existsByEmployeeCode(
                request.getEmployeeCode())) {

            throw new RuntimeException(
                    "Employee code already exists");
        }

        User manager = null;

        if (request.getManagerId() != null) {

            manager = userRepository
                    .findById(
                            request.getManagerId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Manager not found"));
        }

        User user = User.builder()
                .employeeCode(
                        request.getEmployeeCode())
                .fullName(
                        request.getFullName())
                .email(
                        request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()))
                .role(
                        request.getRole() != null
                                ? request.getRole()
                                : Role.EMPLOYEE)
                .manager(manager)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser =
                userRepository.save(user);

        return UserResponseDto.builder()
                .id(savedUser.getId())
                .employeeCode(savedUser.getEmployeeCode())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .managerId(
                        savedUser.getManager() != null
                                ? savedUser.getManager().getId()
                                : null)
                .active(savedUser.getActive())
                .build();
    }
    

    public void forgotPassword(
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        String otp =
                String.valueOf(
                        (int)(
                                100000 +
                                Math.random()
                                        * 900000
                        )
                );

        user.setOtp(otp);

        user.setOtpExpiry(
                LocalDateTime.now()
                        .plusMinutes(10)
        );

        userRepository.save(user);

        emailService.sendOtpEmail(
                user.getEmail(),
                user.getFullName(),
                otp
        );
    }
    
 
    public boolean verifyOtp(
            String email,
            String otp) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return otp.equals(
                user.getOtp()
        ) &&
        user.getOtpExpiry()
                .isAfter(
                        LocalDateTime.now()
                );
    }
    
    public void resetPassword(
            String email,
            String otp,
            String newPassword) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        if(!verifyOtp(email, otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        user.setOtp(null);

        user.setOtpExpiry(null);

        userRepository.save(user);
    }
    
    public void changePassword(
            Long userId,
            ChangePasswordDto request) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Old password is incorrect");
        }

        if (!request.getNewPassword()
                .equals(
                        request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Passwords do not match");
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);
    }
}
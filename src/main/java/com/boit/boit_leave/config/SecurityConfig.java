package com.boit.boit_leave.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                		
                		.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()
                        
                        // Public endpoints for secure email action tokens//
                        .requestMatchers(
                                "/api/leave-action/**"
                        ).permitAll()
                       

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/manager/**")
                        .hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/leave/apply")
                        .hasRole("EMPLOYEE")

                        .requestMatchers(
                                "/api/leave/**")
                        .authenticated()
                        
                        .requestMatchers("/api/reports/**")
                        .hasRole("ADMIN")
                        
                        .requestMatchers("/api/ai/**")
                        .permitAll()
                        
                        .requestMatchers("/api/notifications/**")
                        .authenticated()
                        
                        .requestMatchers("/api/holidays/**")
                        .authenticated()
                        
                        .requestMatchers(
                        	    "/uploads/profile-images/**"
                        	).permitAll()
                        
                        .requestMatchers("/api/appraisals/**")
                        .hasRole("ADMIN")
                        
//                        .requestMatchers(
//                        	    "/uploads/**"
//                        	).permitAll()

                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        config.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",   // web dev (Vite)
                        "http://localhost",        // Capacitor Android (http)
                        "https://localhost",       // Capacitor Android (androidScheme=https)
                        "capacitor://localhost",   // Capacitor iOS
                        "ionic://localhost",
                        "https://leavex-ui.onrender.com"
                        // add your production web domain here, e.g. "https://leave.yourcompany.com"
                ));

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        config.setAllowedHeaders(
                List.of("*"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }
}

package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="country_code")
    private String countryCode;

    @Column(name="country_name")
    private String countryName;

    private Boolean active;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}


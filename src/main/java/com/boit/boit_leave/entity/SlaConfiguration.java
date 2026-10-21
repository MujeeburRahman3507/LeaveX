package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sla_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sla_duration", nullable = false)
    @Builder.Default
    private Integer slaDuration = 3;

    @Column(name = "sla_unit", nullable = false, length = 20)
    @Builder.Default
    private String slaUnit = "DAYS";

    @Column(name = "calculation_type", nullable = false, length = 30)
    @Builder.Default
    private String calculationType = "WORKING_DAYS";

    @Column(name = "warning_before", nullable = false)
    @Builder.Default
    private Integer warningBefore = 1;

    @Column(name = "token_expiry_same_as_sla", nullable = false)
    @Builder.Default
    private Boolean tokenExpirySameAsSla = true;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
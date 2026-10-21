package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "backdated_leave_configuration")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackdatedLeaveConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_backdated_days", nullable = false)
    @Builder.Default
    private Integer maxBackdatedDays = 30;

    @Column(name = "medical_document_required", nullable = false)
    @Builder.Default
    private Boolean medicalDocumentRequired = true;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}
package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type", nullable = false)
    private String actionType;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "action_by")
//    private User actionBy;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_by")
    private User actionBy;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "action_details", columnDefinition = "TEXT")
    private String actionDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}

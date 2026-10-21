package com.boit.boit_leave.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "birthday_wish_log",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_birthday_once",
                        columnNames = {
                                "user_id",
                                "wish_date"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayWishLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "wish_date",
            nullable = false
    )
    private LocalDate wishDate;

    @Column(
            name = "email_sent",
            nullable = false
    )
    @Builder.Default
    private Boolean emailSent = false;

    @Column(
            name = "notification_sent",
            nullable = false
    )
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

}
package com.dlnl.deliveryguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sms_send_history")
@EntityListeners(AuditingEntityListener.class)
public class SmsSendHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_contact", nullable = false)
    private String customerContact;

    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;

    @Column(name = "send_status", nullable = false)
    private String sendStatus;

    @ManyToOne
    @JoinColumn(name = "send_condition_id")
    private SmsSendCondition sendCondition;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
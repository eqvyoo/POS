package com.dlnl.deliveryguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sms_send_condition")
@EntityListeners(AuditingEntityListener.class)
public class SmsSendCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "send_condition", nullable = false)
    private String sendCondition;

    @Column(name = "message_content", nullable = false)
    private String messageContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public void updateUser(User user){
        this.user = user;
    }

    public void updateSendCondition(String sendCondition){
        this.sendCondition = sendCondition;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMessageContent(String messageContent){
        this.messageContent = messageContent;
        this.updatedAt = LocalDateTime.now();
    }
}

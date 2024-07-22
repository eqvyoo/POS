package com.dlnl.deliveryguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "is_sub_valid")
    private Boolean isSubValid;

    @Column(name = "sub_expired_at")
    private Date subExpiredAt;

    @Column(name = "created_at",nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateUpdatedAt(LocalDateTime now){
        this.updatedAt = now;
    }

    public void updateSubExpiredAt(Date subExpiredAt) {
        this.subExpiredAt = subExpiredAt;
    }

    public void updateIsSubValid(Boolean isSubValid){
        this.isSubValid = isSubValid;
    }
}

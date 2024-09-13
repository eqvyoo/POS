package com.dlnl.deliveryguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Time;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_date_time")
    private LocalDateTime orderDateTime;
    @Column(name = "order_menu")
    private String orderMenu;
    @Column(name = "order_phone_number")
    private String orderPhoneNumber;
    @Column(name = "order_platform")
    private String orderPlatform;
    @Column(name = "order_method")
    private String orderMethod;
    @Column(name = "order_type")
    private String orderType;
    @Column(name = "order_status")
    private String orderStatus;
    @Column(name = "payment_amount")
    private String paymentAmount;
    @Column(name = "estimated_cooking_time")
    private Time estimatedCookingTime;
    @Column(name = "delivery_agency")
    private String deliveryAgency;
    @Column(name = "rider_request_time")
    private LocalDateTime riderRequestTime;


}

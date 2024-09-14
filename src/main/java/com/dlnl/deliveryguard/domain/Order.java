package com.dlnl.deliveryguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<Menu> menus;
    @Column(name = "customer_phone_number")
    private String CustomerPhoneNumber;
    @Column(name = "order_platform")
    private String orderPlatform;
    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "order_type")
    private String orderType;
    @Column(name = "status")
    private String status;
    @Column(name = "payment_amount")
    private String paymentAmount;
    @Column(name = "estimated_cooking_time")
    private Time estimatedCookingTime;
    @Column(name = "delivery_agency")
    private String deliveryAgency;
    @Column(name = "rider_request_time")
    private LocalDateTime riderRequestTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;
}
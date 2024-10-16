package com.dlnl.deliveryguard.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderMenu> orderMenus = new ArrayList<>();
    @Column(name = "customer_phone_number")
    private String CustomerPhoneNumber;
    @Column(name = "order_platform")
    private String orderPlatform;
    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "payment_amount")
    private String paymentAmount;
    @Column(name = "estimated_cooking_time")
    private Time estimatedCookingTime;
    @Column(name = "delivery_agency")
    private String deliveryAgency;
    @Column(name = "rider_request_time")
    private LocalDateTime riderRequestTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 고객과의 관계
    private Customer customer; // 주문을 한 고객

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // Store와의 관계
    private Store store; // 주문이 속한 Store
}
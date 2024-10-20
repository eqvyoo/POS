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
    @Builder.Default
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
    @Column(name = "delivery_agency", nullable = true)
    private String deliveryAgency;
    @Column(name = "rider_request_time", nullable = true)
    private LocalDateTime riderRequestTime;

    @Column (name = "delivery_id", nullable = true)
    private String deliveryId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // 고객과의 관계
    private Customer customer; // 주문을 한 고객

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // Store와의 관계
    private Store store; // 주문이 속한 Store

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")  // 배달 주소
    private Address address;  // 주문에 할당된 주소
    @Column(name = "receipt_data")
    private String receiptData;

    @Column(name = "cancel_reason", nullable = true)
    private String cancelReason;

    @Column(name = "pickup_in")
    private Integer pickupIn;   //vroong api의 픽업요청 시간, 단위 : 초, 픽업요청시간. 주문접수시각으로부터 offset 시간 (주문 후 조리완료 및 주문 준비 완료에 걸리는 시간)! 픽업요청가능한 시간 값을 고려해서 배송요청을 하셔야 합니다.

    @Column(name = "contactless")
    private boolean contactless;    // 비대면 배송 여부

    public void updateStatus(Status canceled) {
        this.status = canceled;
    }

    public void updateCancelReason(String reason){
        this.cancelReason = reason;
    }

    public void updateDeliveryAgency(String deliveryAgency){
        this.deliveryAgency = deliveryAgency;
    }

    public void updatePickupIn(Integer time){
        this.pickupIn = time;
    }
    public void updateRiderRequestTime(LocalDateTime time){
        this.riderRequestTime = time;
    }

    public void updateDeliveryId(String id){
        this.deliveryId = id;
    }
}
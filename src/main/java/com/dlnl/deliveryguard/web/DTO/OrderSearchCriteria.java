package com.dlnl.deliveryguard.web.DTO;

import com.dlnl.deliveryguard.domain.OrderType;
import com.dlnl.deliveryguard.domain.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
public class OrderSearchCriteria {
    private LocalDateTime orderDateTime;   // 주문 일시
    private String menuName;               // 메뉴명
    private String customerPhoneNumber;    // 고객 연락처
    private String orderNumber;            // 주문 번호
    private String orderPlatform;          // 주문 플랫폼
    private String paymentMethod;          // 결제 방식
    private OrderType orderType;              // 주문 유형
    private Status status;                 // 처리 상태
    private String paymentAmount;          // 결제 금액
}
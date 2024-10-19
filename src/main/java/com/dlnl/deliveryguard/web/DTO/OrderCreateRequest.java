package com.dlnl.deliveryguard.web.DTO;

import com.dlnl.deliveryguard.domain.OrderType;
import com.dlnl.deliveryguard.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    private LocalDateTime orderDateTime;   // 주문 일시
    private List<MenuItem> menus;          // 메뉴명과 수량 리스트
    private String customerPhoneNumber;    // 고객 연락처
    private String orderNumber;            // 주문 번호
    private String orderPlatform;          // 주문 플랫폼
    private String paymentMethod;          // 결제 방식
    private OrderType orderType;           // 주문 유형 (DELIVERY or PICKUP)
    private Status status;                 // 처리 상태 (PROCESSING, COMPLETED, etc.)
    private String paymentAmount;          // 결제 금액
    private String receiptData;            // 영수증 데이터
    private String address;                // 배달 주소

    private String deliveryId;              // 배달 ID

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItem {
        private String menuName;  // 메뉴명
        private Integer quantity; // 수량
    }
}
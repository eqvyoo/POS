package com.dlnl.deliveryguard.web.DTO;

import com.dlnl.deliveryguard.domain.OrderType;
import com.dlnl.deliveryguard.domain.Status;
import lombok.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class OrderListResponse {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MenuWithQuantity{
        private String menuName;
        private Integer quantity;
    }
    private LocalDateTime orderDateTime;
    private List<MenuWithQuantity> menus;
    private String customerPhoneNumber;
    private String customerNickname;  // 추가된 필드: 고객의 닉네임
    private String customerID;        // 추가된 필드: 고객의 ID
    private String orderNumber;
    private String orderPlatform;
    private String paymentMethod;
    private OrderType orderType;
    private Status status;
    private String paymentAmount;
    private String address;
    private Time estimatedCookingTime;
    private String deliveryAgency;
    private LocalDateTime riderRequestTime;
}

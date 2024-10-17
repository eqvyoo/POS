package com.dlnl.deliveryguard.web.DTO;

import com.dlnl.deliveryguard.domain.OrderType;
import com.dlnl.deliveryguard.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuWithQuantity {
        private String menuName;
        private Integer quantity;
    }

    private LocalDateTime orderDateTime;
    private List<MenuWithQuantity> menus;
    private String customerPhoneNumber;
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

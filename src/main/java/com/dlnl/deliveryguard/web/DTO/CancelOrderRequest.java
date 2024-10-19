package com.dlnl.deliveryguard.web.DTO;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    private Long orderId;
    private String cancelReason;
}
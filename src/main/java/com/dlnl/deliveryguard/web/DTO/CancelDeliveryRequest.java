package com.dlnl.deliveryguard.web.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDeliveryRequest {
    private String deliveryId;

}
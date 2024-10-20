package com.dlnl.deliveryguard.web.DTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
@Builder
public class RiderCallRequest {

    private Long orderId;
    private String deliveryAgency;
    private Integer pickupIn;
}

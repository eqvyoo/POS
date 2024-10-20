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

    private String orderId;
    private String delivery_deliveryAgency;
    private LocalDateTime riderRequestTime;
}

package com.dlnl.deliveryguard.web.DTO;

import lombok.*;

import java.sql.Time;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcceptOrderRequest {
    private Time estimatedCookingTime;  // 조리 예상 시간
}
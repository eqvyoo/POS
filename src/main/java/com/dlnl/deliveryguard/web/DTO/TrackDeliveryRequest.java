package com.dlnl.deliveryguard.web.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackDeliveryRequest {
    private String deliveryId;  // 배달 ID
}
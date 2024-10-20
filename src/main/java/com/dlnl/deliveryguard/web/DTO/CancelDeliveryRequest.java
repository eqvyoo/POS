package com.dlnl.deliveryguard.web.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDeliveryRequest {

    @JsonProperty("delivery_id")
    private String deliveryId;
}
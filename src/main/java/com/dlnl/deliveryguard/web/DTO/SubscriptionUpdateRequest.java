package com.dlnl.deliveryguard.web.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpdateRequest {
    private Long userId;
    private Boolean isSubValid;
    private Date subExpiredAt;
}
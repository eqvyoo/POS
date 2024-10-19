package com.dlnl.deliveryguard.web.DTO;

import com.jayway.jsonpath.EvaluationListener;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDeliveryResponse {
    private String result;
    private String errorType;
    private String errorCode;
    private String errorMessage;
    private String deliveryId;
    private String status;

}


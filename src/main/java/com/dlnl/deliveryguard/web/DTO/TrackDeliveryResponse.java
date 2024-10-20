package com.dlnl.deliveryguard.web.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackDeliveryResponse {
    private String result;  // SUCCESS 또는 FAILED
    private String errorType;
    private String errorCode;
    private String errorMessage;
    private String deliveryId;
    private String status;
    private Long submittedAt;
    private Long agentAssignedAt;
    private Long agentPickedUpAt;
    private Long completedAt;
    private Long canceledAt;
    private Long returnSubmittedAt;
    private String agentName;
    private String agentPhone;
    private String agentLat;
    private String agentLng;
    private Integer agentDistanceToOrigin;
    private Boolean contactless;
}
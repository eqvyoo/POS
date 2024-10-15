package com.dlnl.deliveryguard.web.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@Builder
public class CustomerDetailResponse {
    private String customerID;
    private String nickname;
    private String phoneNumber;
    private List<String> addresses;
}

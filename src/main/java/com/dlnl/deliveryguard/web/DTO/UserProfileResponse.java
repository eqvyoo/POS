package com.dlnl.deliveryguard.web.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private String userName;
    private String phoneNumber;
    private String email;
    private String storeName;
    private String storeAddress;
}
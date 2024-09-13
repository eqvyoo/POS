package com.dlnl.deliveryguard.web.DTO;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    private String loginID;
    private String password;
    private String username;
    private String phoneNumber;
    private String email;
    private String storeName;
    private String storeAddress;
}

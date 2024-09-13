package com.dlnl.deliveryguard.web.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String loginID;
    private String password;
}

package com.dlnl.deliveryguard.web.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateRequest {
    private String username;
    private String newPassword;
}
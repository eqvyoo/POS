package com.dlnl.deliveryguard.web.DTO;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}

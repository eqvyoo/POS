package com.dlnl.deliveryguard.web;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse {
    public String accessToken;
    public String refreshToken;
}

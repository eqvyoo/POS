package com.dlnl.deliveryguard.web;

import lombok.Builder;

@Builder
public class LoginResponse {
    public String accessToken;
    public String refreshToken;
}
